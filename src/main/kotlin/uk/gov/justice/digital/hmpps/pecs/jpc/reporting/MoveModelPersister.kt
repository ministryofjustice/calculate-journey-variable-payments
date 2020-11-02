package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

@Component
class MoveModelPersister(private val moveModelRepository: MoveModelRepository, private val journeyModelRepository: JourneyModelRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(params: FilterParams, moves: List<Report>) {
        logger.info("Persisting ${moves.size} moves")

        val completedMoves = ReportFilterer.completedMoves(params, moves).toList()
        val cancelledBillableMoves = ReportFilterer.cancelledBillableMoves(params, moves).toList()
        val completedAndCancelledMoves = completedMoves + cancelledBillableMoves

        MovePriceType.values().forEach { mpt ->
            logger.info("Persisting $mpt moves")
            mpt.filterer(params, completedAndCancelledMoves).forEach { report ->
                with(report.move) {
                    Result.runCatching {
                        val moveId = report.move.id

                        val pickUp = Event.getLatestByType(report.events, EventType.MOVE_START)?.occurredAt
                        val dropOff = Event.getLatestByType(report.events, EventType.MOVE_COMPLETE)?.occurredAt
                        val cancelled = Event.getLatestByType(report.events, EventType.MOVE_CANCEL)?.occurredAt

                        // delete any existing move / journeys
                        if (moveModelRepository.existsById(moveId)) moveModelRepository.deleteById(moveId)

                        // create new move model
                        val moveModel = MoveModel(
                                moveId = moveId,
                                supplier = Supplier.valueOf(supplier.toUpperCase()),
                                status = MoveStatus.valueOf(status.toUpperCase()),
                                movePriceType = mpt,
                                reference = reference,
                                moveDate = moveDate,
                                fromNomisAgencyId = fromNomisAgencyId,
                                toNomisAgencyId = toNomisAgencyId,
                                pickUpDateTime = pickUp,
                                dropOffOrCancelledDateTime = dropOff ?: cancelled,
                                vehicleRegistration = report.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                                    it.value.journey.vehicleRegistration ?: ""
                                },
                                notes = report.events.notes(),
                                prisonNumber = report.person?.prisonNumber
                        )

                        // add journeys to move model
                        val journeyModels = report.journeysWithEvents.map { journeyModel(moveModel.moveId, it) }
                        moveModel.addJourneys(*journeyModels.toTypedArray())

                        val saved = moveModelRepository.save(moveModel)

                    }.onFailure { logger.warn(it.message) }
                }
            }
        }
    }


    fun journeyModel(moveId: String, journeyWithEvents: JourneyWithEvents): JourneyModel {

        val journeyId = journeyWithEvents.journey.id

        with(journeyWithEvents) {
            val pickUp = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt
            val isCancelled = journeyWithEvents.journey.state == JourneyState.CANCELLED.value

            return JourneyModel(
                    journeyId = journeyId,
                    moveId = moveId,
                    state = JourneyState.valueOf(journey.state.toUpperCase()),
                    fromNomisAgencyId = journey.fromNomisAgencyId,
                    toNomisAgencyId = journey.toNomisAgencyId,
                    pickUpDateTime = pickUp,
                    dropOffDateTime = dropOff,
                    billable = journey.billable,
                    vehicleRegistration = journey.vehicleRegistration,
                    notes = journeyWithEvents.events.notes(),
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.
joinToString() {"${it.type}: ${it.notes}"}

