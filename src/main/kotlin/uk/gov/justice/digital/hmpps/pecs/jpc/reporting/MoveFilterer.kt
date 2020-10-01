package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

object MoveFilterer {

    private fun bySupplier(m: MovePersonJourneysEvents, s: Supplier) = m.move.supplier == s.reportingName()

    private fun byMoveStatus(m: MovePersonJourneysEvents, status: MoveStatus) = m.move.status == status.value

    private fun byEventInDateRange(events: List<Event>, eventType: EventType, dateFrom: LocalDate, dateTo: LocalDate) =
            with(events.find { it.hasType(eventType)}?.occurredAt?.toLocalDate()) {
                this != null && isAfter(dateFrom.minusDays(1)) && isBefore(dateTo.plusDays(1))
            }

    fun completedMoves(params: MoveFiltererParams, moves: Collection<MovePersonJourneysEvents>): Sequence<MovePersonJourneysEvents> {
        return moves.asSequence().filter {
            bySupplier(it, params.supplier) &&
            byEventInDateRange(it.events, EventType.MOVE_COMPLETE, params.movesFrom, params.movesTo) &&
            byMoveStatus(it, MoveStatus.COMPLETED)
        }
    }

    /**
     * A standard move is a completed move with a single completed journey that is billable, and no cancelled journeys
     * To be priced as a standard move, the journey as well as the move must be completed
     * There also should be no redirects after the move starts, but shouldn't need to check for this
     */
    fun standardMoves(params: MoveFiltererParams, moves: Collection<MovePersonJourneysEvents>): Sequence<MovePersonJourneysEvents> {
        return completedMoves(params, moves).filter {
                with(it.journeysWithEvents.map { it.journey }) {
                    count { it.hasState(JourneyState.COMPLETED) } == 1 &&
                    count { it.hasState(JourneyState.COMPLETED) && it.billable } == 1 &&
                    count { it.hasState(JourneyState.CANCELLED) } == 0
            }
        }
    }

    /**
     * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
     * exactly one move redirect event that happened after the move started
     */
    fun simpleRedirectMoves(params: MoveFiltererParams, allMoves: Collection<MovePersonJourneysEvents>): Sequence<MovePersonJourneysEvents> {
        return completedMoves(params, allMoves).filter { m ->
            val moveStartDate = m.events.find { it.type == EventType.MOVE_START.value }?.occurredAt
            m.events.count { it.hasType(EventType.MOVE_REDIRECT) && it.occurredAt.isAfter(moveStartDate) } == 1
            with(m.journeysWithEvents.map {it.journey}) {
                count { it.hasState(JourneyState.COMPLETED, JourneyState.CANCELLED) && it.billable } == 2
            }
        }
    }
}

data class MoveFiltererParams(
        val supplier: Supplier,
        val movesFrom: LocalDate,
        val movesTo: LocalDate,
)