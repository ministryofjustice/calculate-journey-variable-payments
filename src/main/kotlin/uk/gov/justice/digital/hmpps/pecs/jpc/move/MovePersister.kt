package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate

@Component
class MovePersister(private val moveRepository: MoveRepository, private val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(reports: List<Report>) {

        var counter = 1
        logger.info("Persisting ${reports.size} moves")

        reports.forEach { report ->

            Result.runCatching {
                val moveId = report.move.id

                val pickUp = Event.getLatestByType(report.moveEvents, EventType.MOVE_START)?.occurredAt
                val dropOff = Event.getLatestByType(report.moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
                val cancelled = Event.getLatestByType(report.moveEvents, EventType.MOVE_CANCEL)?.occurredAt

                val maybeExistingMove = moveRepository.findById(moveId).orElse(null)

                val mergedReport = maybeExistingMove?.let { existingMove ->
                    // merge move events
                    val mergedMoveEvents = (existingMove.events + report.moveEvents).distinctBy { it.id }

                    // merge journeys and their events
                    val existingJourneys = existingMove.journeys
                    val newJourneys = report.journeysWithEvents.map { reportJourneyWithEventsToJourney(existingMove.moveId, existingMove.moveDate, it) }

                    val mergedJourneys =
                        existingJourneys.filterNot { ej -> newJourneys.any { ej.journeyId == it.journeyId } } +
                            newJourneys.map { nj ->
                                existingJourneys.find { it.journeyId == nj.journeyId }?.let {
                                    nj.copy(events = (nj.events + it.events).distinctBy { it.id }.toMutableSet())
                                } ?: nj
                            }.toMutableSet()

                    report.copy(
                            moveEvents = mergedMoveEvents,
                            journeysWithEvents = mergedJourneys.map { journeyToReportJourneyWithEvents(it) })
                } ?: report

                with(mergedReport.move) {
                    val newMove = Move(
                        moveId = moveId,
                        profileId = profileId,
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
                        prisonNumber = maybeExistingMove?.prisonNumber,
                        latestNomisBookingId = maybeExistingMove?.latestNomisBookingId,
                        dateOfBirth = maybeExistingMove?.dateOfBirth,
                        firstNames = maybeExistingMove?.firstNames,
                        lastName = maybeExistingMove?.lastName,
                        gender =  maybeExistingMove?.gender,
                        ethnicity = maybeExistingMove?.ethnicity,
                        journeys = mergedReport.journeysWithEvents.map { reportJourneyWithEventsToJourney(moveId, moveDate, it) }.toMutableSet(),
                        events = mergedReport.moveEvents.toMutableSet()
                    )

                    moveRepository.save(newMove)

                    if (counter++ % 1000 == 0) {
                        logger.info("Persisted $counter moves out of ${reports.size} (flushing moves to the database).")
                        moveRepository.flush()
                    }
                }

            }.onFailure { logger.warn(it.message) }

            moveRepository.flush()
        }
    }

    fun journeyToReportJourneyWithEvents(journey: Journey): JourneyWithEvents {
        with(journey) {
            return JourneyWithEvents(
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

    fun reportJourneyWithEventsToJourney(moveId: String, moveDate: LocalDate?, journeyWithEvents: JourneyWithEvents): Journey {
        with(journeyWithEvents) {

            val pickUp = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt

            return Journey(
                journeyId = journeyWithEvents.reportJourney.id,
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
                notes = journeyWithEvents.events.notes(),
                events = events.toMutableSet(),
                effectiveYear = pickUp?.year ?: effectiveYearForDate(moveDate ?: timeSource.date())
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }
