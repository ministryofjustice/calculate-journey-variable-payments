package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@Component
class MovePersister(private val moveRepository: MoveRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(reports: List<Report>) {
        logger.info("Persisting ${reports.size} moves")

        var counter = 0
        var output = StringBuilder()

        reports.forEach { report ->
            output.append("${report.move.reference} ")

            Result.runCatching {
            with(report.move) {
                val moveId = report.move.id

                val pickUp = Event.getLatestByType(report.moveEvents, EventType.MOVE_START)?.occurredAt
                val dropOff = Event.getLatestByType(report.moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
                val cancelled = Event.getLatestByType(report.moveEvents, EventType.MOVE_CANCEL)?.occurredAt

                val maybeExistingReport = moveRepository.findById(moveId)
                val mergedReport = if (maybeExistingReport.isPresent) {
                    val existingMove = maybeExistingReport.get()

                    // merge move events
                    val mergedMoveEvents = (existingMove.events + report.moveEvents).distinctBy { it.id }

                    // merge journeys and their events
                    val existingJourneys = existingMove.journeys
                    val newJourneys = report.journeysWithEvents.map { reportJourneyWithEventsToJourney(existingMove.moveId, it) }

                    val mergedJourneys =
                            existingJourneys.filterNot { ej -> newJourneys.any { ej.journeyId == it.journeyId } } +
                                    newJourneys.map { nj ->
                                        existingJourneys.find { it.journeyId == nj.journeyId }?.let {
                                            nj.copy(events = (nj.events + it.events).distinctBy { it.id }.toMutableSet())
                                        } ?: nj
                                    }.toMutableSet()
                    report.copy(moveEvents = mergedMoveEvents, journeysWithEvents = mergedJourneys.map { journeyToReportJourneyWithEvents(it) })
                } else {
                    report
                }

                with(mergedReport.move) {
                    val newMove = Move(
                            moveId = moveId,
                            updatedAt = updatedAt,
                            supplier = Supplier.valueOfCaseInsensitive(supplier),
                            status = MoveStatus.valueOfCaseInsensitive(status),
                            moveType = mergedReport.moveType(),
                            reference = reference,
                            moveDate = moveDate,
                            fromNomisAgencyId = fromNomisAgencyId,
                            toNomisAgencyId = toNomisAgencyId,
                            pickUpDateTime = pickUp,
                            dropOffOrCancelledDateTime = dropOff ?: cancelled,
                            vehicleRegistration = report.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                                it.value.reportJourney.vehicleRegistration ?: ""
                            },
                            notes = report.moveEvents.notes(),
                            prisonNumber = report.person?.prisonNumber,
                            latestNomisBookingId = report.person?.latestNomisBookingId,
                            firstNames = report.person?.firstNames,
                            lastName = report.person?.lastName,
                            dateOfBirth = report.person?.dateOfBirth,
                            ethnicity = report.person?.ethnicity,
                            gender = report.person?.gender,
                            journeys = mergedReport.journeysWithEvents.map { reportJourneyWithEventsToJourney(moveId, it) }.toMutableSet(),
                            events = mergedReport.moveEvents.toMutableSet()
                    )

//                    logger.info("Saving new move ${newMove.reference}")
                    moveRepository.save(newMove)

                    if (counter++ % 500 == 0) {
                        logger.info("Persisted $counter moves out of ${reports.size} (flushing moves to the database).")
                        logger.info("Persisted $output")
                        output.clear()

                        moveRepository.flush()
                    }
                }
            }
            }.onFailure { logger.warn(it.message) }

            moveRepository.flush()
        }
    }

    fun journeyToReportJourneyWithEvents(journey: Journey): ReportJourneyWithEvents{
        with(journey) {
        return ReportJourneyWithEvents(
           reportJourney = ReportJourney(
                id = journeyId,
                updatedAt = updatedAt,
                moveId = moveId,
                billable = billable,
                state = state.name,
                supplier = supplier.name,
                clientTimestamp = clientTimeStamp,
                vehicleRegistration = vehicleRegistration,
                fromNomisAgencyId = fromNomisAgencyId,
                toNomisAgencyId = toNomisAgencyId
            ),
            events = journey.events.toList())
        }
    }

    fun reportJourneyWithEventsToJourney(moveId: String, reportJourneyWithEvents: ReportJourneyWithEvents): Journey {
        with(reportJourneyWithEvents) {

            val pickUp = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt

            return Journey(
                 journeyId = reportJourneyWithEvents.reportJourney.id,
                 supplier = Supplier.valueOfCaseInsensitive(reportJourney.supplier),
                 clientTimeStamp = reportJourney.clientTimestamp,
                 updatedAt = reportJourney.updatedAt,
                 moveId = moveId,
                 state = JourneyState.valueOfCaseInsensitive(reportJourney.state),
                 fromNomisAgencyId = reportJourney.fromNomisAgencyId,
                 toNomisAgencyId = reportJourney.toNomisAgencyId,
                 pickUpDateTime = pickUp,
                 dropOffDateTime = dropOff,
                 billable = reportJourney.billable,
                 vehicleRegistration = reportJourney.vehicleRegistration,
                 notes = reportJourneyWithEvents.events.notes(),
                 events = events.toMutableSet()
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }

