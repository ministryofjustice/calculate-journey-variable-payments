package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun cannedEvent(): Event{
    val occurredAndRecordedAt = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)
    val event = Event(
            id=UUID.fromString("00699c3d-b4db-4e9d-858c-36f81aa19815"),
            type="MoveCancel",
            actionedBy="serco",
            eventableType="move",
            eventableId=UUID.fromString("02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e"),
            details= mapOf("cancellation_reason" to "made_in_error", "cancellation_reason_comment" to "cancelled because the prisoner refused to move"),
            occurredAt=occurredAndRecordedAt,
            recordedAt=occurredAndRecordedAt,
            notes="")
    return event
}
class EventsParserTest {

    @Test
    fun `Assert Event can be created from json`() {
        val eventJson = """{"id": "00699c3d-b4db-4e9d-858c-36f81aa19815", "type": "MoveCancel", "actioned_by": "serco", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}"""
        val expectedEvent = cannedEvent()

        val parsedEvent = Event.fromJson(eventJson)
        Assertions.assertEquals(expectedEvent, parsedEvent)
    }

}