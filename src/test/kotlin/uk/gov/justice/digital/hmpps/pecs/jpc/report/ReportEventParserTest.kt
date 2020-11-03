package uk.gov.justice.digital.hmpps.pecs.jpc.report

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.ReportEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.report.reportMoveEventFactory


class ReportEventParserTest {

    @Test
    fun `Assert Event can be created from json`() {
        val expectedEvent = reportMoveEventFactory()
        val eventJson = """{"id": "ME1", "type": "MoveCancel", "supplier": "serco", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"string_key": "string_val", "int_key": 3, "bool_key" : true}}"""

        val parsedEvent = ReportEvent.fromJson(eventJson)
        Assertions.assertEquals(expectedEvent, parsedEvent)
    }

}