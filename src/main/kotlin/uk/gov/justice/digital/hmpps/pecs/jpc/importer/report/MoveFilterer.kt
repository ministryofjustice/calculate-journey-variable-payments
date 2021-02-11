package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move.Companion.CANCELLATION_REASON_CANCELLED_BY_PMU
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveStatus

object MoveFilterer {

  private val logger = LoggerFactory.getLogger(javaClass)

  private fun List<Event>.hasEventType(eventType: EventType) = this.find { it.hasType(eventType) } != null

  private fun isCompleted(move: Move) = move.status == MoveStatus.completed

  /**
   * For a cancelled move to be billable it must be a previously accepted prison to prison move in a cancelled state.
   * It must have a cancellation reason of cancelled_by_pmu and have been cancelled after 3pm the day before the move date
   */
  fun isCancelledBillableMove(move: Move) =
    move.status == MoveStatus.cancelled &&
      CANCELLATION_REASON_CANCELLED_BY_PMU == move.cancellationReason &&
      move.reportFromLocationType == "prison" &&
      move.toNomisAgencyId != null &&
      move.reportToLocationType == "prison" &&
      move.events.hasEventType(EventType.MOVE_ACCEPT) && // it was previously accepted
      move.events.hasEventType(EventType.MOVE_CANCEL) && // it was cancelled
      move.moveDate != null && move.events.find {
      it.hasType(EventType.MOVE_CANCEL)
    }?.occurredAt?.plusHours(9)?.isAfter(move.moveDate?.atStartOfDay()) ?: false

  /**
   * A standard move is a completed move with a single completed journey that is billable, the journey from and to
   * destinations should match that of the move, and no cancelled journeys. To be priced as a standard move, the journey
   * as well as the move must be completed. There also should be no redirects after the move starts, but shouldn't need
   * to check for this.
   */
  fun isStandardMove(move: Move) =
    isCompleted(move) &&
      with(move.journeys) {
        count { it.stateIsAnyOf(JourneyState.completed) } == 1 &&
          count { isCompleteBillableJourneyAndLocationsMatchMove(it, move) } == 1 &&
          count { it.stateIsAnyOf(JourneyState.cancelled) } == 0
      }

  private fun isCompleteBillableJourneyAndLocationsMatchMove(journey: Journey, move: Move) =
    journey.state == JourneyState.completed && journey.billable &&
      journey.fromNomisAgencyId == move.fromNomisAgencyId &&
      journey.toNomisAgencyId == move.toNomisAgencyId

  /**
   * A simple lodging move must be a completed move with one journey lodging event OR 1 move lodging start and 1 move lodging end event
   * It must also have at 2 billable, completed journeys
   */
  fun isLongHaulMove(move: Move) =
    isCompleted(move) &&
      (
        move.hasAllOf(EventType.JOURNEY_LODGING) ||
          move.hasAllOf(EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END)
        ) &&
      move.hasNoneOf(EventType.MOVE_REDIRECT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
      with(move.journeys.map { it }) {
        count { it.stateIsAnyOf(JourneyState.completed) && it.billable } == 2
      }

  /**
   * A simple lockout move must be a completed move with one journey lockout event OR 1 move lockout event
   * And no redirect event. It must also have 2 or 3 completed, billable journeys
   */
  fun isLockoutMove(move: Move) =
    isCompleted(move) &&
      move.hasAnyOf(EventType.MOVE_LOCKOUT, EventType.JOURNEY_LOCKOUT) &&
      move.hasNoneOf(EventType.MOVE_REDIRECT) &&
      with(move.journeys) {
        count { it.stateIsAnyOf(JourneyState.completed) && it.billable } in 2..3
      }

  /**
   * All other completed moves not covered by standard, redirect, long haul or lockout moves
   */
  fun isMultiTypeMove(move: Move) = isCompleted(move) &&
    !isStandardMove(move) &&
    !isRedirectionMove(move) &&
    !isLongHaulMove(move) &&
    !isLockoutMove(move)

  /**
   * A simple redirect is a completed move with 2 billable (cancelled or completed) journeys and
   * exactly one move redirect event that happened after the move started
   * If there is no move start event, it logs a warning and continues
   */
  fun isRedirectionMove(move: Move) =
    isCompleted(move) &&
      move.hasAnyOf(EventType.MOVE_REDIRECT) &&
      move.hasNoneOf(
        EventType.JOURNEY_LODGING,
        EventType.MOVE_LODGING_START,
        EventType.MOVE_LODGING_END,
        EventType.MOVE_LOCKOUT,
        EventType.JOURNEY_LOCKOUT
      ) &&
      when (val moveStartDate = move.events.find { it.type == EventType.MOVE_START.value }?.occurredAt) {
        null -> {
          logger.warn("No move start date event found for move $move")
          false
        }
        else -> {
          move.events.count { it.hasType(EventType.MOVE_REDIRECT) && it.occurredAt.isAfter(moveStartDate) } == 1 &&
            with(move.journeys) {
              count { it.stateIsAnyOf(JourneyState.completed, JourneyState.cancelled) && it.billable } == 2
            }
        }
      }
}
