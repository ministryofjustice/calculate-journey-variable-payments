package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Event

class EventParserTest {

  @Test
  fun `Assert Event can be created from json`() {
    val expectedEvent = moveEventFactory()
    val eventJson =
      """{"id": "ME1", "type": "MoveCancel", "supplier": "SERCO", "notes": "", "updated_at": "2020-06-16T10:20:30+01:00", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"string_key": "string_val", "int_key": 3, "bool_key" : true}}"""

    val parsedEvent = Event.fromJson(eventJson)
    Assertions.assertEquals(expectedEvent, parsedEvent)
  }
}
