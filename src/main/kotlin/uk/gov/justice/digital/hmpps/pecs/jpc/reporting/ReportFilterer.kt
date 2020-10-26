package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.output.ClosedRangeLocalDate
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Move.Companion.CANCELLATION_REASON_CANCELLED_BY_PMU
import java.time.LocalDate
import java.time.LocalDateTime

object ReportFilterer {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun Report.hasSupplier(s: Supplier) = move.supplier == s.reportingName()

    private fun Report.hasStatus(status: MoveStatus) = move.status == status.value

    private fun List<Event>.hasEventType(eventType: EventType) =
            this.find { it.hasType(eventType) } != null

    private fun List<Event>.hasEventTypeInDateRange(eventType: EventType, dateRange: ClosedRangeLocalDate) =
            with(this.find { it.hasType(eventType) }?.occurredAt?.toLocalDate()) {
                this != null && dateRange.contains(this)
            }

    fun completedMoves(params: FilterParams, reports: Collection<Report>): Sequence<Report> {
        return reports.asSequence().filter {
            it.hasSupplier(params.supplier) &&
            it.hasStatus(MoveStatus.COMPLETED) &&
            it.events.hasEventTypeInDateRange(EventType.MOVE_COMPLETE, params.dateRange())
        }
    }

    /**
     * For a cancelled move to be billable it must be a previously accepted prison to prison move in a cancelled state.
     * It must have a cancellation reason of cancelled_by_pmu and have been cancelled after 3pm the day before the move date
     */
    fun cancelledBillableMoves(params: FilterParams, reports: Collection<Report>): Sequence<Report> {
        return reports.asSequence().filter {
            it.hasSupplier(params.supplier) &&
            it.hasStatus(MoveStatus.CANCELLED) &&
            CANCELLATION_REASON_CANCELLED_BY_PMU == it.move.cancellationReason &&
            it.move.fromLocationType == "prison" &&
            it.move.toNomisAgencyId != null &&
            it.move.toLocationType == "prison" &&
            it.events.hasEventType(EventType.MOVE_ACCEPT) && // it was previously accepted
            it.events.hasEventTypeInDateRange(EventType.MOVE_CANCEL, params.dateRange()) && // cancel event within date range
            it.events.find{it.hasType(EventType.MOVE_CANCEL)}?.occurredAt?.plusHours(9)?.isAfter(it.move.moveDate?.atStartOfDay()) ?: false
        }.map { it.copy(journeysWithEvents = listOf(JourneyWithEvents( // fake journey with events
                Journey(
                        id = "FAKE",
                        moveId = it.move.id,
                        billable = true,
                        supplier = it.move.supplier,
                        clientTimestamp = LocalDateTime.now(),
                        fromNomisAgencyId = it.move.fromNomisAgencyId,
                        toNomisAgencyId = it.move.toNomisAgencyId!!,
                        state = JourneyState.CANCELLED.value,
                        vehicleRegistration = null
                ),
                listOf())))
        }
    }

    /**
     * A standard move is a completed move with a single completed journey that is billable, and no cancelled journeys
     * To be priced as a standard move, the journey as well as the move must be completed
     * There also should be no redirects after the move starts, but shouldn't need to check for this
     */
    fun standardMoveReports(params: FilterParams, reports: Collection<Report>): Sequence<Report> {
        return completedMoves(params, reports).filter { report ->
            with(report.journeysWithEvents.map { it.journey }) {
                count { it.stateIsAnyOf(JourneyState.COMPLETED) } == 1 &&
                count { it.stateIsAnyOf(JourneyState.COMPLETED) && it.billable } == 1 &&
                count { it.stateIsAnyOf(JourneyState.CANCELLED) } == 0
            }
        }
    }

    /**
     * A simple lodging move must be a completed move with one journey lodging event OR 1 move lodging start and 1 move lodging end event
     * It must also have at 2 billable, completed journeys
     */
    fun longHaulReports(params: FilterParams, moves: Collection<Report>): Sequence<Report> {
        return completedMoves(params, moves).filter { report ->
            report.hasAnyOf(EventType.JOURNEY_LODGING, EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END) &&
            report.hasNoneOf(EventType.MOVE_REDIRECT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
            with(report.journeysWithEvents.map { it.journey }) {
                count { it.stateIsAnyOf(JourneyState.COMPLETED) && it.billable } == 2
            }
        }
    }

    /**
     * A simple lockout move must be a completed move with one journey lockout event OR 1 move lockout event
     * And no redirect event. It must also have 2 or 3 completed, billable journeys
     */
    fun lockoutReports(params: FilterParams, moves: Collection<Report>): Sequence<Report> {
        return completedMoves(params, moves).filter { report ->
            report.hasAnyOf(EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
                    report.hasNoneOf(EventType.MOVE_REDIRECT) &&
                    with(report.journeysWithEvents.map { it.journey }) {
                        count { it.stateIsAnyOf(JourneyState.COMPLETED) && it.billable } in 2..3
                    }
        }   
    }

    /**
     * All other completed reports not covered by standard, redirect, long haul or lockout reports
     */
    fun multiTypeReports(params: FilterParams, moves: Collection<Report>): Sequence<Report> {
        return (
                completedMoves(params, moves) - (
                    standardMoveReports(params, moves) +
                    redirectionReports(params, moves) +
                    longHaulReports(params, moves) +
                    lockoutReports(params, moves)
                )).asSequence()
    }

    /**
     * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
     * exactly one move redirect event that happened after the move started
     * If there is no move start event, it logs a warning and continues
     */
    fun redirectionReports(params: FilterParams, moves: Collection<Report>): Sequence<Report> {
        return completedMoves(params, moves).filter { report ->
            report.hasAnyOf(EventType.MOVE_REDIRECT) &&
            report.hasNoneOf(
                    EventType.JOURNEY_LODGING, EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END,
                    EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT
            ) &&
            when (val moveStartDate = report.events.find { it.type == EventType.MOVE_START.value }?.occurredAt) {
                null -> {
                    logger.warn("No move start date event found for move $report")
                    false
                }
                else -> {
                    report.events.count { it.hasType(EventType.MOVE_REDIRECT) && it.occurredAt.isAfter(moveStartDate) } == 1 &&
                    with(report.journeysWithEvents.map { it.journey }) {
                        count { it.stateIsAnyOf(JourneyState.COMPLETED, JourneyState.CANCELLED) && it.billable } == 2
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