package uk.gov.justice.digital.hmpps.pecs.jpc.domain.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.moveEventFactory
import java.time.LocalDateTime

internal class EventTest {

  @Test
  fun `get latest by type returns the latest event`() {
    val moveCancelFirst = moveEventFactory() // MOVE_CANCEL
    val moveCancelLast = moveCancelFirst.copy(eventId = "ME2", occurredAt = moveCancelFirst.occurredAt.plusHours(3))

    assertThat(Event.getLatestByType(listOf(moveCancelLast, moveCancelFirst), EventType.MOVE_CANCEL)).isEqualTo(
      moveCancelLast,
    )
  }

  @Test
  fun `get latest by type returns null if event not present`() {
    val moveCancel = moveEventFactory() // MOVE_CANCEL event

    assertThat(Event.getLatestByType(listOf(moveCancel), EventType.MOVE_COMPLETE)).isNull()
  }

  @Test
  fun `can parse GEOAmey journey start JSON to event`() {
    val event = """
      {"id": "JE1", "updated_at": "2022-02-15T12:00:00", "type": "JourneyStart", "supplier": "geoamey", "notes": "Note for GEO Journey Start event",  "eventable_id": "J1", "eventable_type": "journey", "occurred_at": "2022-02-15T12:00:00", "recorded_at": "2022-02-15T12:00:00", "details": {"vehicle_reg": "ABC"}}
    """.trimIndent()

    with(Event.fromJson(event)!!) {
      assertThat(eventId).isEqualTo("JE1")
      assertThat(updatedAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(type).isEqualTo("JourneyStart")
      assertThat(supplier).isEqualTo(Supplier.GEOAMEY)
      assertThat(notes).isEqualTo("Note for GEO Journey Start event")
      assertThat(eventableId).isEqualTo("J1")
      assertThat(eventableType).isEqualTo("journey")
      assertThat(occurredAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(recordedAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(details).isEqualTo(Details(mapOf("vehicle_reg" to "ABC")))
    }
  }

  @Test
  fun `can parse Serco journey complete JSON to event`() {
    val event = """
      {"id": "JE2", "updated_at": "2022-02-15T12:00:00", "type": "JourneyComplete", "supplier": "serco", "notes": "Note for Serco Journey Start event",  "eventable_id": "J2", "eventable_type": "journey", "occurred_at": "2022-02-15T12:00:00", "recorded_at": "2022-02-15T12:00:00", "details": {"vehicle_reg": "ABC"}}
    """.trimIndent()

    with(Event.fromJson(event)!!) {
      assertThat(eventId).isEqualTo("JE2")
      assertThat(updatedAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(type).isEqualTo("JourneyComplete")
      assertThat(supplier).isEqualTo(Supplier.SERCO)
      assertThat(notes).isEqualTo("Note for Serco Journey Start event")
      assertThat(eventableId).isEqualTo("J2")
      assertThat(eventableType).isEqualTo("journey")
      assertThat(occurredAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(recordedAt).isEqualTo(LocalDateTime.of(2022, 2, 15, 12, 0, 0))
      assertThat(details).isEqualTo(Details(mapOf("vehicle_reg" to "ABC")))
    }
  }
}
