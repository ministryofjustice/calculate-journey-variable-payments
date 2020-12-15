package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportMove.Companion.CANCELLATION_REASON_CANCELLED_BY_PMU
import uk.gov.justice.digital.hmpps.pecs.jpc.price.equalsStringCaseInsensitive
import java.time.LocalDateTime

object ReportFilterer {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun Report.hasStatus(status: MoveStatus) = status.equalsStringCaseInsensitive(move.status)

    private fun List<Event>.hasEventType(eventType: EventType) = this.find { it.hasType(eventType) } != null

    private fun completedMoves(reports: Collection<Report>) = reports.asSequence().filter { it.hasStatus(MoveStatus.COMPLETED) }

    /**
     * For a cancelled move to be billable it must be a previously accepted prison to prison move in a cancelled state.
     * It must have a cancellation reason of cancelled_by_pmu and have been cancelled after 3pm the day before the move date
     */
    fun cancelledBillableMoves(reports: Collection<Report>): Sequence<Report> {
        return reports.asSequence().filter { report ->
            report.hasStatus(MoveStatus.CANCELLED) &&
            CANCELLATION_REASON_CANCELLED_BY_PMU == report.move.cancellationReason &&
            report.move.fromLocationType == "prison" &&
            report.move.toNomisAgencyId != null &&
            report.move.toLocationType == "prison" &&
            report.moveEvents.hasEventType(EventType.MOVE_ACCEPT) && // it was previously accepted
            report.moveEvents.hasEventType(EventType.MOVE_CANCEL) && // it was cancelled
            report.move.moveDate != null && report.moveEvents.find{
                it.hasType(EventType.MOVE_CANCEL)}?.occurredAt?.plusHours(9)?.isAfter(report.move.moveDate.atStartOfDay()) ?: false
        }.map { it.copy(journeysWithEvents = listOf(ReportJourneyWithEvents( // fake journey with events
                ReportJourney(
                        id = "FAKE",
                        updatedAt = LocalDateTime.now(),
                        moveId = it.move.id,
                        billable = true,
                        supplier = it.move.supplier,
                        clientTimestamp = LocalDateTime.now(),
                        fromNomisAgencyId = it.move.fromNomisAgencyId,
                        toNomisAgencyId = it.move.toNomisAgencyId!!,
                        state = JourneyState.CANCELLED.name,
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
    fun standardMoveReports(reports: Collection<Report>): Sequence<Report> {
        return completedMoves(reports).filter { report ->
            with(report.journeysWithEvents.map { it.reportJourney }) {
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
    fun longHaulReports(moves: Collection<Report>): Sequence<Report> {
        return completedMoves(moves).filter { report ->
            (
                report.hasAllOf(EventType.JOURNEY_LODGING) ||
                report.hasAllOf(EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END)
            ) &&
            report.hasNoneOf(EventType.MOVE_REDIRECT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
            with(report.journeysWithEvents.map { it.reportJourney }) {
                count { it.stateIsAnyOf(JourneyState.COMPLETED) && it.billable } == 2
            }
        }
    }

    /**
     * A simple lockout move must be a completed move with one journey lockout event OR 1 move lockout event
     * And no redirect event. It must also have 2 or 3 completed, billable journeys
     */
    fun lockoutReports(moves: Collection<Report>): Sequence<Report> {
        return completedMoves(moves).filter { report ->
            report.hasAnyOf(EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
                    report.hasNoneOf(EventType.MOVE_REDIRECT) &&
                    with(report.journeysWithEvents.map { it.reportJourney }) {
                        count { it.stateIsAnyOf(JourneyState.COMPLETED) && it.billable } in 2..3
                    }
        }   
    }

    /**
     * All other completed reports not covered by standard, redirect, long haul or lockout reports
     */
    fun multiTypeReports(moves: Collection<Report>): Sequence<Report> {
        return (
                completedMoves(moves) - (
                    standardMoveReports(moves) +
                    redirectionReports(moves) +
                    longHaulReports(moves) +
                    lockoutReports(moves)
                )).asSequence()
    }

    /**
     * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
     * exactly one move redirect event that happened after the move started
     * If there is no move start event, it logs a warning and continues
     */
    fun redirectionReports(moves: Collection<Report>): Sequence<Report> {
        return completedMoves(moves).filter { report ->
            report.hasAnyOf(EventType.MOVE_REDIRECT) &&
            report.hasNoneOf(
                    EventType.JOURNEY_LODGING, EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END,
                    EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT
            ) &&
            when (val moveStartDate = report.moveEvents.find { it.type == EventType.MOVE_START.value }?.occurredAt) {
                null -> {
                    logger.warn("No move start date event found for move $report")
                    false
                }
                else -> {
                    report.moveEvents.count { it.hasType(EventType.MOVE_REDIRECT) && it.occurredAt.isAfter(moveStartDate) } == 1 &&
                    with(report.journeysWithEvents.map { it.reportJourney }) {
                        count { it.stateIsAnyOf(JourneyState.COMPLETED, JourneyState.CANCELLED) && it.billable } == 2
                    }
                }
            }
        }
    }
}