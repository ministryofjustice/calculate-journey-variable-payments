package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.output.ClosedRangeLocalDate
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

object MoveReportFilterer {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun MoveReport.hasSupplier(s: Supplier) = move.supplier == s.reportingName()

    private fun MoveReport.hasStatus(status: MoveStatus) = move.status == status.value

    private fun List<Event>.hasEventInDateRange(eventType: EventType, from: LocalDate, to: LocalDate) =
            with(this.find { it.hasType(eventType) }?.occurredAt?.toLocalDate()) {
                this != null && isAfter(from.minusDays(1)) && isBefore(to.plusDays(1))
            }

    private fun completedMoves(params: FilterParams, reports: Collection<MoveReport>): Sequence<MoveReport> {
        return reports.asSequence().filter {
            it.hasSupplier(params.supplier) &&
            it.hasStatus(MoveStatus.COMPLETED) &&
            it.events.hasEventInDateRange(EventType.MOVE_COMPLETE, params.movesFrom, params.movesTo)
        }
    }

    /**
     * A standard move is a completed move with a single completed journey that is billable, and no cancelled journeys
     * To be priced as a standard move, the journey as well as the move must be completed
     * There also should be no redirects after the move starts, but shouldn't need to check for this
     */
    fun standardMoveReports(params: FilterParams, reports: Collection<MoveReport>): Sequence<MoveReport> {
        return completedMoves(params, reports).filter { report ->
            with(report.journeysWithEvents.map { it.journey }) {
                count { it.hasState(JourneyState.COMPLETED) } == 1 &&
                        count { it.hasState(JourneyState.COMPLETED) && it.billable } == 1 &&
                        count { it.hasState(JourneyState.CANCELLED) } == 0
            }
        }
    }

    /**
     * A simple lodging move must be a completed move with one journey lodging event OR 1 move lodging start and 1 move lodging end event
     * It must also have 2 billable, completed journeys
     */
    fun lodgingReports(params: FilterParams, moves: Collection<MoveReport>): Sequence<MoveReport> {
        return completedMoves(params, moves).filter { report ->
            report.hasEvent(EventType.JOURNEY_LODGING) || (report.hasEvent(EventType.MOVE_LODGING_START) && report.hasEvent(EventType.MOVE_LODGING_END)) &&
            with(report.journeysWithEvents.map { it.journey }) {
                count { it.hasState(JourneyState.COMPLETED) && it.billable } == 2
            }
        }
    }

    /**
     * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
     * exactly one move redirect event that happened after the move started
     * If there is no move start event, it logs a warning and continues
     */
    fun redirectionReports(params: FilterParams, moves: Collection<MoveReport>): Sequence<MoveReport> {
        return completedMoves(params, moves).filter { report ->
            if(report.hasEvent(EventType.MOVE_REDIRECT)) logger.info("***REDIRECT EVENT***$report")
            when (val moveStartDate = report.events.find { it.type == EventType.MOVE_START.value }?.occurredAt) {
                null -> {
                    logger.warn("No move start date event found for move $report")
                    false
                }
                else -> {
                    report.events.count { it.hasType(EventType.MOVE_REDIRECT) && it.occurredAt.isAfter(moveStartDate) } == 1 &&
                    with(report.journeysWithEvents.map { it.journey }) {
                        count { it.hasState(JourneyState.COMPLETED, JourneyState.CANCELLED) && it.billable } == 2
                    }
                }
            }
        }
    }
}

data class FilterParams(
        val supplier: Supplier,
        val movesFrom: LocalDate,
        val movesTo: LocalDate) {

    fun dateRange() = ClosedRangeLocalDate(movesFrom, movesTo)
}