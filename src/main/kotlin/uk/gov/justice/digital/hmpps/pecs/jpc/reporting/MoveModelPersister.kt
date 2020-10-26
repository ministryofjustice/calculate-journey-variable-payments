package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.output.notes
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.util.*

@Component
class MoveModelPersister(private val locationRepository: LocationRepository, private val moveModelRepository: MoveModelRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(params: FilterParams, moves: List<Report>) {

        val completedMoves = ReportFilterer.completedMoves(params, moves).toList()
        val cancelledBillableMoves = ReportFilterer.cancelledBillableMoves(params, moves).toList()
        val completedAndCancelledMoves = completedMoves + cancelledBillableMoves

        MovePriceType.values().forEach { mpt ->
            mpt.filterer(params, completedAndCancelledMoves).forEach { report ->
                with(report.move) {
                    Result.runCatching {
                        val moveId = report.move.id

                        val pickUp = Event.getLatestByType(report.events, EventType.MOVE_START)?.occurredAt
                        val dropOff = Event.getLatestByType(report.events, EventType.MOVE_COMPLETE)?.occurredAt
                        val cancelled = Event.getLatestByType(report.events, EventType.MOVE_CANCEL)?.occurredAt

                        // delete any existing move / journeys
                        if (moveModelRepository.existsById(moveId)) moveModelRepository.deleteById(moveId)

                        // get new from location and to location
                        val fromLocation = locationRepository.findByNomisAgencyId(report.move.fromNomisAgencyId)
                        val toLocation = report.move.toNomisAgencyId?.let { locationRepository.findByNomisAgencyId(it) }

                        // create new move model
                        val moveModel = MoveModel(
                                moveId = moveId,
                                supplier = Supplier.valueOf(supplier.toUpperCase()),
                                status = MoveStatus.valueOf(status.toUpperCase()),
                                movePriceType = mpt,
                                reference = reference,
                                moveDate = moveDate,
                                fromNomisAgencyId = fromNomisAgencyId,
                                fromLocation = fromLocation,
                                toNomisAgencyId = toNomisAgencyId,
                                toLocation = toLocation,
                                pickUp = pickUp,
                                dropOffOrCancelled = dropOff ?: cancelled,
                                vehicleRegistration = report.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                                    it.value.journey.vehicleRegistration ?: "NOT GIVEN"
                                },
                                notes = report.events.notes(),
                                prisonNumber = report.person?.prisonNumber
                        )

                        // add journeys to move model
                        val journeyModels = report.journeysWithEvents.map { journeyModel(moveModel, it) }
                        moveModel.addJourneys(*journeyModels.toTypedArray())

                        val saved = moveModelRepository.save(moveModel)
                        logger.info("Saved: " + saved)

                    }.onFailure { logger.warn(it.message) }
                }
            }
        }
    }


    fun journeyModel(moveModel: MoveModel, journeyWithEvents: JourneyWithEvents): JourneyModel {

        val journeyId = journeyWithEvents.journey.id

        with(journeyWithEvents) {
            val pickUp = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
            val dropOff = Event.getLatestByType(journeyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt
            val isCancelled = journeyWithEvents.journey.state == JourneyState.CANCELLED.value

            // get new from location and to location
            val fromLocation = locationRepository.findByNomisAgencyId(journey.fromNomisAgencyId)
            val toLocation = locationRepository.findByNomisAgencyId(journey.toNomisAgencyId)

            return JourneyModel(
                    journeyId = journeyId,
                    move = moveModel,
                    state = JourneyState.valueOf(journey.state.toUpperCase()),
                    fromNomisAgencyId = journey.fromNomisAgencyId,
                    fromLocation = fromLocation,
                    toNomisAgencyId = journey.toNomisAgencyId,
                    toLocation = toLocation,
                    pickUp = pickUp,
                    dropOff = dropOff,
                    billable = journey.billable,
                    vehicleRegistation = journey.vehicleRegistration,
                    notes = journeyWithEvents.events.notes()
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.
joinToString() {"${it.type}: ${it.notes}"}

