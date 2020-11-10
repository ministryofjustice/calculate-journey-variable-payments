package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportJourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Report
import java.time.LocalDate

internal class ReportTest {

    val date: LocalDate = LocalDate.now()

    val moveAcceptEvent = reportMoveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(24))
    val moveRedirectEvent = reportMoveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M9")
    val moveCancelEvent = reportMoveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(2))

    val journeyStartEvent = reportJourneyEventFactory(type = EventType.JOURNEY_START.value)
    val journeyCancelEvent = reportJourneyEventFactory(type = EventType.JOURNEY_CANCEL.value)

    val report = Report(
            reportMove = reportMoveFactory(),
            reportPerson = reportPersonFactory(),
            reportEvents = listOf(moveAcceptEvent, moveRedirectEvent, moveCancelEvent),
            journeysWithEventReports = listOf(ReportJourneyWithEvents(reportJourneyFactory(), listOf(journeyStartEvent, journeyCancelEvent)))
    )

    @Test
    fun `has any of`() {
        assertThat(report.hasAnyOf(EventType.MOVE_ACCEPT)).isTrue
    }

    @Test
    fun `has all of`() {
        assertThat(report.hasAllOf(EventType.MOVE_ACCEPT, EventType.MOVE_REDIRECT, EventType.MOVE_CANCEL)).isTrue
        assertThat(report.hasAllOf(EventType.MOVE_ACCEPT, EventType.JOURNEY_COMPLETE)).isFalse

    }

    @Test
    fun `has none of`() {
        assertThat(report.hasNoneOf(EventType.MOVE_ACCEPT)).isFalse
    }

    @Test
    fun `get events`() {
        assertThat(report.getEvents(EventType.MOVE_ACCEPT, EventType.MOVE_CANCEL, EventType.JOURNEY_START)).containsExactlyInAnyOrder(moveAcceptEvent, moveCancelEvent, journeyStartEvent)
    }
}