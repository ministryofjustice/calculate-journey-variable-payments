package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportJourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@Component
class MovePersister(private val moveRepository: MoveRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(params: FilterParams, reports: List<Report>) {
        logger.info("Persisting ${reports.size} moves")

        reports.forEach { report ->
            val moveType = report.moveType(params)
            with(report.move) {
                Result.runCatching {
                    val moveId = report.move.id

                    val pickUp = Event.getLatestByType(report.moveEvents, EventType.MOVE_START)?.occurredAt
                    val dropOff = Event.getLatestByType(report.moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
                    val cancelled = Event.getLatestByType(report.moveEvents, EventType.MOVE_CANCEL)?.occurredAt

                    val (moveEvents, moveJourneys) = if (moveRepository.existsById(moveId)) {
                        val existingMove = moveRepository.findById(moveId).get()

                        // merge move events
                        val mergedMoveEvents = (existingMove.events + report.moveEvents).distinctBy { it.id }

                        // merge journeys journeys
                        val existingJourneys = existingMove.journeys
                        val newJourneys = report.journeysWithEvents.map { journeyModel(existingMove.moveId, it) }

                        val mergedJourneys =
                                existingJourneys.filterNot { ej -> newJourneys.any { ej.journeyId == it.journeyId } } +
                                        newJourneys.map { nj ->
                                            existingJourneys.find { it.journeyId == nj.journeyId }?.let {
                                                nj.copy(events = (nj.events + it.events).distinctBy { it.id }.toMutableList())
                                            } ?: nj
                                        }.toMutableList()
                        Pair(mergedMoveEvents, mergedJourneys)
                    } else {
                        Pair(report.moveEvents, report.journeysWithEvents.map { journeyModel(report.move.id, it) })
                    }

                    val newMove = Move(
                            moveId = moveId,
                            updatedAt = updatedAt,
                            supplier = Supplier.valueOfCaseInsensitive(supplier),
                            status = MoveStatus.valueOfCaseInsensitive(status),
                            moveType = moveType,
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
                            events = moveEvents.toMutableList(),
                            journeys = moveJourneys.toMutableList()
                    )

                    moveRepository.save(newMove)

                }.onFailure { logger.warn(it.message) }
            }
        }

    }


    fun journeyModel(moveId: String, reportJourneyWithEvents: ReportJourneyWithEvents): Journey {

        val journeyId = reportJourneyWithEvents.reportJourney.id

        with(reportJourneyWithEvents) {
            val pickUp = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt

            return Journey(
                    journeyId = journeyId,
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
                    events = events.toMutableList()
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }

