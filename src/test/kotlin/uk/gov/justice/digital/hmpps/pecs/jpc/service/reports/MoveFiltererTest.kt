package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import java.time.LocalDate

internal class MoveFiltererTest {

  val from = LocalDate.of(2020, 9, 10)
  val to = LocalDate.of(2020, 9, 11)

  private val standard = reportMoveFactory(
    events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
    journeys = listOf(reportJourneyFactory(journeyId = "J1M1", billable = true)),
  )

  private val cancelled = reportMoveFactory(
    moveId = "M2",
    status = MoveStatus.cancelled,
    events = listOf(
      moveEventFactory(
        type = EventType.MOVE_CANCEL.value,
        moveId = "M2",
        occurredAt = to.atStartOfDay(),
      ),
    ),
    journeys = listOf(reportJourneyFactory(journeyId = "J1M2", moveId = "M2", billable = true)),
  )

  private val completedUnbillable = reportMoveFactory(
    moveId = "M4",
    events = listOf(
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M4",
        occurredAt = from.atStartOfDay(),
      ),
    ),
    journeys = listOf(reportJourneyFactory(journeyId = "J1M4", moveId = "M4", billable = false)),
  )

  private val completedRedirection = reportMoveFactory(
    moveId = "M5",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M5", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_REDIRECT.value,
        moveId = "M5",
        occurredAt = from.atStartOfDay().plusHours(2),
      ),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M5",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M5", moveId = "M5", billable = true),
      reportJourneyFactory(journeyId = "J2M5", moveId = "M5", billable = true),
    ),
  )

  private val completedLongHaulMoveLodgingEvents = reportMoveFactory(
    moveId = "M6",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M6", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M6",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
      moveEventFactory(
        type = EventType.MOVE_LODGING_START.value,
        moveId = "M6",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
      moveEventFactory(
        type = EventType.MOVE_LODGING_END.value,
        moveId = "M6",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M6", moveId = "M6", billable = true),
      reportJourneyFactory(journeyId = "J2M6", moveId = "M6", billable = true),
    ),
  )

  private val completedLongHaulJourneyLodgingEvents = reportMoveFactory(
    moveId = "M6a",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M6a", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M6a",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
      moveEventFactory(
        type = EventType.MOVE_LODGING_START.value,
        moveId = "M6a",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
      moveEventFactory(
        type = EventType.MOVE_LODGING_END.value,
        moveId = "M6a",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),

    ),
    journeys = listOf(
      reportJourneyFactory(
        journeyId = "J1M6a",
        moveId = "M6a",
        billable = true,
        events = listOf(journeyEventFactory(type = EventType.JOURNEY_CANCEL.value)),
      ),
      reportJourneyFactory(journeyId = "J2M6a", moveId = "M6a", billable = true),
    ),
  )

  private val multiTypeMove = reportMoveFactory(
    moveId = "M7",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M7", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_REDIRECT.value,
        moveId = "M7",
        occurredAt = from.atStartOfDay().plusHours(2),
      ),
      moveEventFactory(
        type = EventType.MOVE_LODGING_START.value,
        moveId = "M7",
        occurredAt = from.atStartOfDay().plusHours(2),
      ),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M7",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M6", moveId = "M7", billable = true),
      reportJourneyFactory(journeyId = "J2M6", moveId = "M7", billable = true),
    ),
  )

  private val multiTypeMoveRedirect = reportMoveFactory(
    moveId = "M8",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M7", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_REDIRECT.value,
        moveId = "M8",
        occurredAt = from.atStartOfDay().plusMinutes(1),
      ),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M8",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M8", moveId = "M8", billable = true),
    ),
  )

  private val standardMoveRedirect = reportMoveFactory(
    moveId = "M9",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M9", occurredAt = from.atStartOfDay()),
      moveEventFactory(
        type = EventType.MOVE_REDIRECT.value,
        moveId = "M9",
        occurredAt = from.atStartOfDay().minusMinutes(1),
      ),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M9",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M9", moveId = "M9", billable = true),
    ),
  )

  private val multiTypeMoveWhenSingleBillableJourneyPickUpLocationDoesNotMatch = reportMoveFactory(
    events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
    journeys = listOf(reportJourneyFactory(journeyId = "J1M1", billable = true, fromLocation = "DOES_NOT_MATCH_MOVE")),
  )

  private val multiTypeMoveWhenSingleBillableJourneyDropOffLocationDoesNotMatch = reportMoveFactory(
    events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
    journeys = listOf(reportJourneyFactory(journeyId = "J1M1", billable = true, toLocation = "DOES_NOT_MATCH_MOVE")),
  )

  private val completedLockoutMoveLockoutEvent = reportMoveFactory(
    moveId = "M8b",
    events = listOf(
      moveEventFactory(type = EventType.MOVE_START.value, moveId = "M8b", occurredAt = from.atStartOfDay()),
      moveEventFactory(type = EventType.MOVE_LOCKOUT.value, moveId = "M8b"),
      moveEventFactory(
        type = EventType.MOVE_COMPLETE.value,
        moveId = "M8b",
        occurredAt = from.atStartOfDay().plusHours(4),
      ),
    ),
    journeys = listOf(
      reportJourneyFactory(journeyId = "J1M8b", moveId = "M8b", billable = true),
      reportJourneyFactory(journeyId = "J2M8b", moveId = "M8b", billable = true),
    ),
  )

  private val cancelledBillable = reportMoveFactory(
    moveId = "M9",
    status = MoveStatus.cancelled,
    fromLocation = fromPrisonNomisAgencyId(),
    fromLocationType = "prison",
    toLocation = toCourtNomisAgencyId(),
    toLocationType = "prison",
    cancellationReason = "cancelled_by_pmu",
    date = to,
    events = listOf(
      moveEventFactory(
        type = EventType.MOVE_ACCEPT.value,
        moveId = "M9",
        occurredAt = to.atStartOfDay().minusHours(24),
      ),
      moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = to.atStartOfDay().minusHours(2)),
    ),
    journeys = listOf(),
  )

  private val cancelledIncompletePer = reportMoveFactory(
    moveId = "M10",
    status = MoveStatus.cancelled,
    fromLocation = fromPrisonNomisAgencyId(),
    fromLocationType = "prison",
    toLocation = toCourtNomisAgencyId(),
    toLocationType = "prison",
    cancellationReason = "incomplete_per",
    date = to,
    events = listOf(
      moveEventFactory(
        type = EventType.MOVE_ACCEPT.value,
        moveId = "M10",
        occurredAt = to.atStartOfDay().minusHours(24),
      ),
      moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M10", occurredAt = to.atStartOfDay().minusHours(2)),
    ),
    journeys = listOf(),
  )

  @Test
  fun `is standard move`() {
    assertThat(MoveFilterer.isStandardMove(standard)).isTrue
    assertThat(MoveFilterer.isStandardMove(completedRedirection)).isFalse
  }

  @Test
  fun `is completed redirection`() {
    assertThat(MoveFilterer.isRedirectionMove(completedRedirection)).isTrue
    assertThat(MoveFilterer.isRedirectionMove(standard)).isFalse
  }

  @Test
  fun `is long haul move`() {
    assertThat(MoveFilterer.isLongHaulMove(completedLongHaulMoveLodgingEvents)).isTrue
    assertThat(MoveFilterer.isLongHaulMove(completedLongHaulJourneyLodgingEvents)).isTrue
    assertThat(MoveFilterer.isLongHaulMove(completedRedirection)).isFalse
  }

  @Test
  fun `is lockout move`() {
    assertThat(MoveFilterer.isLockoutMove(completedLockoutMoveLockoutEvent)).isTrue
    assertThat(MoveFilterer.isLockoutMove(standard)).isFalse
  }

  @Test
  fun `is multi-type move`() {
    assertThat(MoveFilterer.isMultiTypeMove(multiTypeMove)).isTrue
    assertThat(MoveFilterer.isMultiTypeMove(completedUnbillable)).isTrue
    assertThat(MoveFilterer.isMultiTypeMove(completedLockoutMoveLockoutEvent)).isFalse
  }

  @Test
  fun `is multi-type move for redirection missing journey`() {
    assertThat(MoveFilterer.isMultiTypeMove(multiTypeMoveRedirect)).isTrue
  }

  @Test
  fun `is standard move with redirection prior to move start`() {
    assertThat(MoveFilterer.isStandardMove(standardMoveRedirect)).isTrue
  }

  @Test
  fun `is multi-type move when single billable journey pick up location does not match`() {
    assertThat(MoveFilterer.isMultiTypeMove(multiTypeMoveWhenSingleBillableJourneyPickUpLocationDoesNotMatch)).isTrue
  }

  @Test
  fun `is multi-type move when single billable journey drop off location does not match`() {
    assertThat(MoveFilterer.isMultiTypeMove(multiTypeMoveWhenSingleBillableJourneyDropOffLocationDoesNotMatch)).isTrue
  }

  @Test
  fun `is cancelled billable`() {
    assertThat(MoveFilterer.isCancelledBillableMove(cancelledBillable)).isTrue
    assertThat(MoveFilterer.isCancelledBillableMove(multiTypeMove)).isFalse
    assertThat(MoveFilterer.isCancelledBillableMove(cancelledIncompletePer)).isTrue
  }

  @Test
  fun `billable cancelled move move type is CANCELLED`() {
    assertThat(cancelledBillable.moveType()).isEqualTo(MoveType.CANCELLED)
  }

  @Test
  fun `non billable cancelled move move type is null`() {
    assertThat(cancelled.moveType()).isNull()
  }

  @Test
  fun `standard move move type is STANDARD`() {
    assertThat(standard.moveType()).isEqualTo(MoveType.STANDARD)
  }
}
