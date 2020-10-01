package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

internal class MoveFiltererTest{

    val from = LocalDate.of(2020, 9, 10)
    val to = LocalDate.of(2020, 9, 11)

    private val standardMoveInDateRange = MovePersonJourneysEvents(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M1", billable = true)))
    )

    private val cancelledMove = MovePersonJourneysEvents(
            move = moveFactory(moveId = "M2"),
            person = personFactory(),
            events =  listOf(moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M2", occurredAt = to.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M2", moveId = "M2", billable = true)))
    )

    private val standardMoveOutsideDateRange = MovePersonJourneysEvents(
            move = moveFactory(moveId = "M3"),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M3", occurredAt = LocalDate.of(2020, 9, 9).atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M3", moveId = "M3", billable = true)))
    )

    private val completedUnbillableMove = MovePersonJourneysEvents(
            move = moveFactory(moveId = "M4"),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M4", occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M4", moveId = "M4", billable = false)))
    )

    private val completedRedirectedMove = MovePersonJourneysEvents(
            move = moveFactory(moveId = "M5"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M5", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M5", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M5", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(journeyId = "J1M5", moveId = "M5", billable = true)),
                    JourneyWithEvents(journeyFactory(journeyId = "J2M5", moveId = "M5", billable = true))
            )
    )

    private val allMoves = listOf(
            standardMoveInDateRange,
            cancelledMove,
            standardMoveOutsideDateRange,
            completedUnbillableMove,
            completedRedirectedMove
    )

    private val moveFilterParams = MoveFiltererParams(Supplier.SERCO, from, to)

    @Test
    fun `Only standard moves within date range are filtered`() {

        val standardMoves = MoveFilterer.standardMoves(moveFilterParams, allMoves).toSet()
        assertThat(standardMoves).isEqualTo(setOf<MovePersonJourneysEvents>(standardMoveInDateRange))
    }

    @Test
    fun `Only standard redirect moves within date range are filtered`() {

        val redirectMoves = MoveFilterer.simpleRedirectMoves(moveFilterParams, allMoves).toSet()
        assertThat(redirectMoves).isEqualTo(setOf<MovePersonJourneysEvents>(completedRedirectedMove))
    }
}