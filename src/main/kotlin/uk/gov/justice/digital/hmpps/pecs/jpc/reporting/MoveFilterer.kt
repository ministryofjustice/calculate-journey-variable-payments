package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

object MoveFilterer {

    /**
     * A standard move is a completed move with a single completed journey that is billable, and no cancelled journeys
     * To be priced as a standard move, the journey as well as the move must be completed
     * There also should be no redirects after the move starts, but shouldn't need to check for this
     */
    fun standardMoves(supplier: Supplier, allMoves: Collection<MovePersonJourneysEvents>): Sequence<MovePersonJourneysEvents> {
        return allMoves.asSequence().filter {
            it.move.supplier == supplier.reportingName() &&
            it.move.status == MoveStatus.COMPLETED.value && with(it.journeysWithEvents) {
                count { it.journey.state == JourneyState.COMPLETED.value } == 1 &&
                        count { it.journey.state == JourneyState.COMPLETED.value && it.journey.billable } == 1 &&
                        count { it.journey.state == JourneyState.CANCELLED.value } == 0
            }
        }
    }

    /**
     * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
     * exactly one move redirect event that happened after the move started
     */
    fun simpleRedirectMoves(supplier: Supplier, allMoves: Collection<MovePersonJourneysEvents>): Collection<MovePersonJourneysEvents> {
        return allMoves.filter {
            val moveStartDate = it.events.find { it.type == EventType.MOVE_START.value}?.occurredAt
            it.move.supplier == supplier.reportingName() &&
            it.move.status == MoveStatus.COMPLETED.value &&
                    it.journeysWithEvents.filter{
                         JourneyState.states.contains(it.journey.state) && it.journey.billable }.size == 2 &&
                    it.events.count{ it.type == EventType.MOVE_REDIRECT.value && it.occurredAt.isAfter(moveStartDate) } == 1
        }
    }
}