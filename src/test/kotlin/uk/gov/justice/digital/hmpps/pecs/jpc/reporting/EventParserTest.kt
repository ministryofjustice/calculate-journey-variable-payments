package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class EventParserTest {

    @Test
    fun `Assert Event can be created from json`() {
        val expectedEvent = moveEventFactory()
        val eventJson = """{"id": "ME1", "type": "MoveCancel", "supplier": "serco", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}"""

        val parsedEvent = Event.fromJson(eventJson)
        Assertions.assertEquals(expectedEvent, parsedEvent)
    }

}