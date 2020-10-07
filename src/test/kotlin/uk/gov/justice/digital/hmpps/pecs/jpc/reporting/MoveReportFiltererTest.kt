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

    private val completedLodging = MoveReport(
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

    private val reports = listOf(
            standardInDateRange,
            cancelled,
            standardOutsideDateRange,
            completedUnbillable,
            completedRedirection,
            completedLodging
    )

    private val filter = FilterParams(Supplier.SERCO, from, to)

    @Test
    fun `Only standard moves within date range are filtered`() {

        val standardReports = MoveReportFilterer.standardMoveReports(filter, reports).toSet()
        assertThat(standardReports).isEqualTo(setOf(standardInDateRange))
    }

    @Test
    fun `Only redirection moves within date range are filtered`() {

        val redirectReports = MoveReportFilterer.redirectionReports(filter, reports).toSet()
        assertThat(redirectReports).isEqualTo(setOf(completedRedirection))
    }

    @Test
    fun `Only lodging moves within date range are filtered`() {

        val lodgingReports = MoveReportFilterer.longHaulReports(filter, reports).toSet()
        assertThat(lodgingReports).isEqualTo(setOf(completedLodging))
    }
}