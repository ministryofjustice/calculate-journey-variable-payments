package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.EventType

internal class EventTest {

  @Test
  fun `get latest by type returns the latest event`() {
    val moveCancelFirst = moveEventFactory() // MOVE_CANCEL
    val moveCancelLast = moveCancelFirst.copy(eventId = "ME2", occurredAt = moveCancelFirst.occurredAt.plusHours(3))

    assertThat(Event.getLatestByType(listOf(moveCancelLast, moveCancelFirst), EventType.MOVE_CANCEL)).isEqualTo(
      moveCancelLast
    )
  }

  @Test
  fun `get latest by type returns null if event not present`() {
    val moveCancel = moveEventFactory() // MOVE_CANCEL event

    assertThat(Event.getLatestByType(listOf(moveCancel), EventType.MOVE_COMPLETE)).isNull()
  }
}
