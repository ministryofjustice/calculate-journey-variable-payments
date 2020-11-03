package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.*
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

        MoveType.values().forEach { mpt ->
            logger.info("Persisting $mpt moves")
            mpt.filterer(params, completedAndCancelledMoves).forEach { report ->
                with(report.reportMove) {
                    Result.runCatching {
                        val moveId = report.reportMove.id

                        val pickUp = ReportEvent.getLatestByType(report.reportEvents, EventType.MOVE_START)?.occurredAt
                        val dropOff = ReportEvent.getLatestByType(report.reportEvents, EventType.MOVE_COMPLETE)?.occurredAt
                        val cancelled = ReportEvent.getLatestByType(report.reportEvents, EventType.MOVE_CANCEL)?.occurredAt

                        // delete any existing move / journeys
                        if (moveRepository.existsById(moveId)) moveRepository.deleteById(moveId)

                        // create new move model
                        val moveModel = Move(
                                moveId = moveId,
                                supplier = Supplier.valueOfCaseInsensitive(supplier),
                                status = MoveStatus.valueOfCaseInsensitive(status),
                                moveType = mpt,
                                reference = reference,
                                moveDate = moveDate,
                                fromNomisAgencyId = fromNomisAgencyId,
                                toNomisAgencyId = toNomisAgencyId,
                                pickUpDateTime = pickUp,
                                dropOffOrCancelledDateTime = dropOff ?: cancelled,
                                vehicleRegistration = report.journeysWithEventReports.withIndex().joinToString(separator = ", ") {
                                    it.value.reportJourney.vehicleRegistration ?: ""
                                },
                                notes = report.reportEvents.notes(),
                                prisonNumber = report.reportPerson?.prisonNumber
                        )

                        // add journeys to move model
                        val journeyModels = report.journeysWithEventReports.map { journeyModel(moveModel.moveId, it) }
                        moveModel.addJourneys(*journeyModels.toTypedArray())

                        val saved = moveRepository.save(moveModel)

                    }.onFailure { logger.warn(it.message) }
                }
            }
        }
    }


    fun journeyModel(moveId: String, reportJourneyWithEvents: ReportJourneyWithEvents): Journey {

        val journeyId = reportJourneyWithEvents.reportJourney.id

        with(reportJourneyWithEvents) {
            val pickUp = ReportEvent.getLatestByType(reportJourneyWithEvents.reportEvents, EventType.JOURNEY_START)?.occurredAt
            val dropOff = ReportEvent.getLatestByType(reportJourneyWithEvents.reportEvents, EventType.JOURNEY_COMPLETE)?.occurredAt
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
                    notes = reportJourneyWithEvents.reportEvents.notes(),
            )
        }
    }

}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<ReportEvent>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.
joinToString {"${it.type}: ${it.notes}"}

