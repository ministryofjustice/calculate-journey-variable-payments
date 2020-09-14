package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Klaxon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Move
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventsParserTest {

    @Test
    fun `Assert Event can be created from json`() {
        val events = getReportLines("/reporting/events.jsonl")

        val occurredAndRecordedAt = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)
        val expectedEvent = Event(
                id=UUID.fromString("00699c3d-b4db-4e9d-858c-36f81aa19815"),
                type="MoveCancel",
                actionedBy="serco",
                eventableType="move",
                eventableId=UUID.fromString("02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e"),
                details= mapOf("cancellation_reason" to "made_in_error", "cancellation_reason_comment" to "cancelled because the prisoner refused to move"),
                occurredAt=occurredAndRecordedAt,
                recordedAt=occurredAndRecordedAt,
                notes="")

        val parsedEvent = Event.fromJson(events[0])
        println(parsedEvent)
        Assertions.assertEquals(expectedEvent, parsedEvent)
    }

}