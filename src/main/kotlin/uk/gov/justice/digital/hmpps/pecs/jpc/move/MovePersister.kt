package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.equalsStringCaseInsensitive

@Component
class MoveModelPersister(private val moveRepository: MoveRepository, private val journeyRepository: JourneyRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(params: FilterParams, moves: List<Report>) {
        logger.info("Persisting ${moves.size} moves")

        val completedMoves = ReportFilterer.completedMoves(params, moves).toList()
        val cancelledBillableMoves = ReportFilterer.cancelledBillableMoves(params, moves).toList()
        val completedAndCancelledMoves = completedMoves + cancelledBillableMoves

        MoveType.values().forEach { mt ->
            logger.info("Persisting $mt moves")
            mt.filterer(params, completedAndCancelledMoves).forEach { report ->
                with(report.move) {
                    Result.runCatching {
                        val moveId = report.move.id

                        val pickUp = Event.getLatestByType(report.moveEvents, EventType.MOVE_START)?.occurredAt
                        val dropOff = Event.getLatestByType(report.moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
                        val cancelled = Event.getLatestByType(report.moveEvents, EventType.MOVE_CANCEL)?.occurredAt

                        val move = moveRepository.findById(moveId)
                        // delete any existing move / journeys
                      //  if (moveRepository.existsById(moveId)) moveRepository.deleteById(moveId)



                        // create new move model
                        val moveModel = Move(
                                moveId = moveId,
                                supplier = Supplier.valueOfCaseInsensitive(supplier),
                                status = MoveStatus.valueOfCaseInsensitive(status),
                                moveType = mt,
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
                                events = report.moveEvents.toMutableList()
                        )

                        // add journeys to move model
                        val journeyModels = report.journeysWithEvents.map { journeyModel(moveModel.moveId, it) }
                        moveModel.addJourneys(*journeyModels.toTypedArray())

                        moveRepository.save(moveModel)

                    }.onFailure { logger.warn(it.message) }
                }
            }
        }
    }


    fun journeyModel(moveId: String, reportJourneyWithEvents: ReportJourneyWithEvents): Journey {

        val journeyId = reportJourneyWithEvents.reportJourney.id

        with(reportJourneyWithEvents) {
            val pickUp = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(reportJourneyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt
            val isCancelled = JourneyState.CANCELLED.equalsStringCaseInsensitive(reportJourneyWithEvents.reportJourney.state)

            return Journey(
                    journeyId = journeyId,
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

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.
joinToString {"${it.type}: ${it.notes}"}

