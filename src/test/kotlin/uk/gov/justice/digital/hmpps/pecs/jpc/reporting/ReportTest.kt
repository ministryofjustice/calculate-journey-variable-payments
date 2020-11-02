package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ReportTest {

    val date: LocalDate = LocalDate.now()

    val moveAcceptEvent = moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(24))
    val moveRedirectEvent = moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M9")
    val moveCancelEvent = moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(2))

    val journeyStartEvent = journeyEventFactory(type = EventType.JOURNEY_START.value)
    val journeyCancelEvent = journeyEventFactory(type = EventType.JOURNEY_CANCEL.value)

    val report = Report(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(moveAcceptEvent, moveRedirectEvent, moveCancelEvent),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(), listOf(journeyStartEvent, journeyCancelEvent)))
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