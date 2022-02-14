package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.EventType
import java.time.LocalDate

internal class ReportJourneyTest {

  val date: LocalDate = LocalDate.now()

  val moveAcceptEvent =
    moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(24))
  val moveRedirectEvent = moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M9")
  val moveCancelEvent =
    moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(2))

  val journeyStartEvent = journeyEventFactory(type = EventType.JOURNEY_START.value)
  val journeyCancelEvent = journeyEventFactory(type = EventType.JOURNEY_CANCEL.value)

  val move = reportMoveFactory(
    events = listOf(moveAcceptEvent, moveRedirectEvent, moveCancelEvent),
    journeys = listOf(reportJourneyFactory(events = listOf(journeyStartEvent, journeyCancelEvent)))
  )

  @Test
  fun `has any of`() {
    assertThat(move.hasAnyOf(EventType.MOVE_ACCEPT)).isTrue
  }

  @Test
  fun `has all of`() {
    assertThat(move.hasAllOf(EventType.MOVE_ACCEPT, EventType.MOVE_REDIRECT, EventType.MOVE_CANCEL)).isTrue
    assertThat(move.hasAllOf(EventType.MOVE_ACCEPT, EventType.JOURNEY_COMPLETE)).isFalse
  }

  @Test
  fun `has none of`() {
    assertThat(move.hasNoneOf(EventType.MOVE_ACCEPT)).isFalse
  }

  @Test
  fun `get events`() {
    assertThat(
      move.getEvents(
        EventType.MOVE_ACCEPT,
        EventType.MOVE_CANCEL,
        EventType.JOURNEY_START
      )
    ).containsExactlyInAnyOrder(moveAcceptEvent, moveCancelEvent, journeyStartEvent)
  }
}
