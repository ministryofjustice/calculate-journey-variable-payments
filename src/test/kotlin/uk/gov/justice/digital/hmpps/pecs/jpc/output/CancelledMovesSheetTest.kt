package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class CancelledMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())
    private val date: LocalDate = LocalDate.now()

    @Test
    internal fun `test cancelled prices`() {
        val movesDate = LocalDate.of(2020, 9, 10)

        val journeyWithEvents = JourneyWithEvents(journeyFactory(billable = true), listOf())
        val cancelledBillable = Report(
                move = moveFactory(
                        moveId = "M9",
                        status = MoveStatus.CANCELLED.value,
                        fromLocation = fromLocationFactory(locationType = LocationType.PR),
                        toLocation = toLocationFactory(locationType = LocationType.PR),
                        cancellationReason = "cancelled_by_pmu",
                        date = date
                ),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(24)),
                        moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = date.atStartOfDay().minusHours(2))
                ),
                journeysWithEvents = listOf()
        )

        val fromLocation = fromLocationFactory(locationType = LocationType.PR)
        val toLocation = toLocationFactory(locationType = LocationType.PR)
        val price = MovePrice(cancelledBillable, listOf(JourneyPrice(journeyWithEvents, 1001)))

        val sms = CancelledMovesSheet(workbook, PriceSheet.Header(movesDate, ClosedRangeLocalDate(movesDate, movesDate), Supplier.SERCO))
        sms.writeMoves(listOf(price))

        assertCellEquals(sms, 10, 0, cancelledBillable.move.reference)
        assertCellEquals(sms, 10, 1, fromLocation.siteName)
        assertCellEquals(sms, 10, 2, fromLocation.locationType.name) // pick up location type

        assertCellEquals(sms, 10, 3, toLocation.siteName)
        assertCellEquals(sms, 10, 4, toLocation.locationType.name) // drop off location type
    }
}