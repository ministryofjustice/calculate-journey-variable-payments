package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

internal class MoveReportFiltererTest{

    val from = LocalDate.of(2020, 9, 10)
    val to = LocalDate.of(2020, 9, 11)

    private val standardInDateRange = MoveReport(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M1", billable = true)))
    )

    private val cancelled = MoveReport(
            move = moveFactory(moveId = "M2"),
            person = personFactory(),
            events =  listOf(moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M2", occurredAt = to.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M2", moveId = "M2", billable = true)))
    )

    private val standardOutsideDateRange = MoveReport(
            move = moveFactory(moveId = "M3"),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M3", occurredAt = LocalDate.of(2020, 9, 9).atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M3", moveId = "M3", billable = true)))
    )

    private val completedUnbillable = MoveReport(
            move = moveFactory(moveId = "M4"),
            person = personFactory(),
            events = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M4", occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J1M4", moveId = "M4", billable = false)))
    )

    private val completedRedirection = MoveReport(
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

    private val completedLongHaul = MoveReport(
            move = moveFactory(moveId = "M6"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M6", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M6", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(journeyId = "J1M6", moveId = "M6", billable = true),
                            listOf(journeyEventFactory(journeyId = "J1M6", type = "JourneyLodging"))),
                    JourneyWithEvents(journeyFactory(journeyId = "J2M6", moveId = "M6", billable = true))
            )
    )


    private val multiTypeMove = MoveReport(
            move = moveFactory(moveId = "M7"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M7", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_LODGING_START.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(journeyId = "J1M6", moveId = "M7", billable = true)),
                    JourneyWithEvents(journeyFactory(journeyId = "J2M6", moveId = "M7", billable = true))
            )
    )

    private val completedLockout = MoveReport(
            move = moveFactory(moveId = "M8"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M8", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M8", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(journeyId = "J1M8", moveId = "M8", billable = true),
                            listOf(journeyEventFactory(journeyId = "J1M8", type = "JourneyLockout"))),
                    JourneyWithEvents(journeyFactory(journeyId = "J2M8", moveId = "M8", billable = true))
            )
    )

    private val reports = listOf(
            standardInDateRange,
            cancelled,
            standardOutsideDateRange,
            completedUnbillable,
            completedRedirection,
            completedLongHaul,
            completedLockout,
            multiTypeMove
    )

    private val filter = FilterParams(Supplier.SERCO, from, to)

    @Test
    fun `Only standard moves within date range are filtered`() {

        val standardReports = MoveReportFilterer.standardMoveReports(filter, reports).toSet()
        assertThat(standardReports).containsOnly(standardInDateRange)
    }

    @Test
    fun `Only redirection moves within date range are filtered`() {

        val redirectReports = MoveReportFilterer.redirectionReports(filter, reports).toSet()
        assertThat(redirectReports).containsOnly(completedRedirection)
    }

    @Test
    fun `Only long haul moves within date range are filtered`() {

        val longHaulReports = MoveReportFilterer.longHaulReports(filter, reports).toSet()
        assertThat(longHaulReports).containsOnly(completedLongHaul)
    }

    @Test
    fun `Only lockout moves within date range are filtered`() {

        val lockoutReports = MoveReportFilterer.lockoutReports(filter, reports).toSet()
        assertThat(lockoutReports).containsOnly(completedLockout)
    }

    @Test
    fun `Unbillable and multi-type moves`() {

        val multiTypeReports = MoveReportFilterer.multiTypeReports(filter, reports).toSet()
        assertThat(multiTypeReports).containsExactlyInAnyOrder(multiTypeMove, completedUnbillable)
    }
}