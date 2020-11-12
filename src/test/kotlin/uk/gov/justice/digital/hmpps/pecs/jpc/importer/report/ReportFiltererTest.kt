package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

internal class ReportFiltererTest {

    val from = LocalDate.of(2020, 9, 10)
    val to = LocalDate.of(2020, 9, 11)

    private val standardInDateRange = Report(
            move = reportMoveFactory(),
            person = personFactory(),
            moveEvents = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M1", billable = true)))
    )

    private val cancelled = Report(
            move = reportMoveFactory(moveId = "M2"),
            person = personFactory(),
            moveEvents =  listOf(moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M2", occurredAt = to.atStartOfDay())),
            journeysWithEvents = listOf(ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M2", moveId = "M2", billable = true)))
    )

    private val standardOutsideDateRange = Report(
            move = reportMoveFactory(moveId = "M3"),
            person = personFactory(),
            moveEvents = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M3", occurredAt = LocalDate.of(2020, 9, 9).atStartOfDay())),
            journeysWithEvents = listOf(ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M3", moveId = "M3", billable = true)))
    )

    private val completedUnbillable = Report(
            move = reportMoveFactory(moveId = "M4"),
            person = personFactory(),
            moveEvents = listOf(moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M4", occurredAt = from.atStartOfDay())),
            journeysWithEvents = listOf(ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M4", moveId = "M4", billable = false)))
    )

    private val completedRedirection = Report(
            move = reportMoveFactory(moveId = "M5"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M5", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M5", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M5", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M5", moveId = "M5", billable = true)),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M5", moveId = "M5", billable = true))
            )
    )

    private val completedLongHaulMoveLodgingEvents = Report(
            move = reportMoveFactory(moveId = "M6"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M6", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M6", occurredAt = from.atStartOfDay().plusHours(4)),
                    moveEventFactory(type = EventType.MOVE_LODGING_START.value, moveId = "M6", occurredAt = from.atStartOfDay().plusHours(4)),
                    moveEventFactory(type = EventType.MOVE_LODGING_END.value, moveId = "M6", occurredAt = from.atStartOfDay().plusHours(4))


            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M6", moveId = "M6", billable = true),
                            listOf()),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M6", moveId = "M6", billable = true))
            )
    )

    private val completedLongHaulJourneyLodgingEvents = Report(
            move = reportMoveFactory(moveId = "M6a"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M6a", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M6a", occurredAt = from.atStartOfDay().plusHours(4)),
                    moveEventFactory(type = EventType.MOVE_LODGING_START.value, moveId = "M6a", occurredAt = from.atStartOfDay().plusHours(4)),
                    moveEventFactory(type = EventType.MOVE_LODGING_END.value, moveId = "M6a", occurredAt = from.atStartOfDay().plusHours(4))


            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M6a", moveId = "M6a", billable = true),
                            listOf(journeyEventFactory(type = EventType.JOURNEY_LODGING.value))),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M6a", moveId = "M6a", billable = true))
            )
    )


    private val multiTypeMove = Report(
            move = reportMoveFactory(moveId = "M7"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M7", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_REDIRECT.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_LODGING_START.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(2)),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M7", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M6", moveId = "M7", billable = true)),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M6", moveId = "M7", billable = true))
            )
    )

    private val completedLockoutJourneyLockoutEvent = Report(
            move = reportMoveFactory(moveId = "M8"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M8", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M8", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M8", moveId = "M8", billable = true),
                            listOf(journeyEventFactory(journeyId = "J1M8", type = "JourneyLockout"))),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M8", moveId = "M8", billable = true))
            )
    )

    private val completedLockoutMoveLockoutEvent = Report(
            move = reportMoveFactory(moveId = "M8b"),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, moveId = "M8b", occurredAt = from.atStartOfDay()),
                    moveEventFactory(type = "MoveLockout", moveId = "M8b"),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, moveId = "M8b", occurredAt = from.atStartOfDay().plusHours(4))
            ),
            journeysWithEvents = listOf(
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J1M8b", moveId = "M8b", billable = true)),
                    ReportJourneyWithEvents(reportJourneyFactory(journeyId = "J2M8b", moveId = "M8b", billable = true))
            )
    )

    private val cancelledBillable = Report(
            move = reportMoveFactory(
                    moveId = "M9",
                    status = MoveStatus.CANCELLED.name.toLowerCase(),
                    fromLocation = fromPrisonNomisAgencyId(),
                    fromLocationType = "prison",
                    toLocation = toCourtNomisAgencyId(),
                    toLocationType = "prison",
                    cancellationReason = "cancelled_by_pmu",
                    date = to
            ),
            person = personFactory(),
            moveEvents = listOf(
                    moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = to.atStartOfDay().minusHours(24)),
                    moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = to.atStartOfDay().minusHours(2))
            ),
            journeysWithEvents = listOf()
    )


    private val reports = listOf(
            standardInDateRange,
            cancelled,
            standardOutsideDateRange,
            completedUnbillable,
            completedRedirection,
            completedLongHaulMoveLodgingEvents,
            completedLongHaulJourneyLodgingEvents,
            completedLockoutJourneyLockoutEvent,
            completedLockoutMoveLockoutEvent,
            multiTypeMove,
            cancelledBillable
    )

    private val filter = FilterParams(Supplier.SERCO, from, to)

    @Test
    fun `Only standard moves within date range are filtered`() {

        val standardReports = ReportFilterer.standardMoveReports(filter, reports).toList()
        assertThat(standardReports).containsOnly(standardInDateRange)
    }

    @Test
    fun `Only redirection moves within date range are filtered`() {

        val redirectReports = ReportFilterer.redirectionReports(filter, reports).toList()
        assertThat(redirectReports).containsOnly(completedRedirection)
    }

    @Test
    fun `Only long haul moves within date range are filtered`() {

        val longHaulReports = ReportFilterer.longHaulReports(filter, reports).toList()
        assertThat(longHaulReports).containsExactlyInAnyOrder(completedLongHaulMoveLodgingEvents, completedLongHaulJourneyLodgingEvents)
    }

    @Test
    fun `Only lockout moves within date range are filtered`() {

        val lockoutReports = ReportFilterer.lockoutReports(filter, reports).toList()
        assertThat(lockoutReports).containsExactlyInAnyOrder(completedLockoutJourneyLockoutEvent, completedLockoutMoveLockoutEvent)
    }

    @Test
    fun `Unbillable and multi-type moves`() {

        val multiTypeReports = ReportFilterer.multiTypeReports(filter, reports).toList()
        assertThat(multiTypeReports).containsExactlyInAnyOrder(multiTypeMove, completedUnbillable)
    }

    @Test
    fun `Cancelled billable moves`() {

        val cancelledBillableReports = ReportFilterer.cancelledBillableMoves(filter, reports).toList()
        assertThat(cancelledBillableReports.map{it.move}).containsExactlyInAnyOrder(cancelledBillable.move)
    }
}
