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
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class CancelledMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `test cancelled prices`() {
        val moveDate = LocalDate.of(2020, 9, 10)
        val move = moveFactory(
                moveId = "M9",
                status = MoveStatus.CANCELLED.value,
                fromLocation = fromPrisonNomisAgencyId(),
                fromLocationType = "prison",
                toLocation = toCourtNomisAgencyId(),
                toLocationType = "prison",
                cancellationReason = "cancelled_by_pmu",
                date = moveDate
        )

        val person = personFactory()

        val journeyWithEvents = JourneyWithEvents(journeyFactory(billable = true), listOf())
        val cancelledBillable = Report(
                move = move,
                person = person,
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = moveDate.atStartOfDay().minusHours(24)),
                        moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", notes = "Cancelled due to snow", occurredAt = moveDate.atStartOfDay().minusHours(2))
                ),
                journeysWithEvents = listOf()
        )

        val price = MovePrice(cancelledBillable, listOf(JourneyPrice(journeyWithEvents, 1001)))

        val sheet = CancelledMovesSheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeMoves(listOf(price))

        assertCellEquals(sheet, 10, 0, cancelledBillable.move.reference)
        assertCellEquals(sheet, 10, 1, "from")
        assertCellEquals(sheet, 10, 2, "PR") // pick up location type

        assertCellEquals(sheet, 10, 3, "to")
        assertCellEquals(sheet, 10, 4, "CO") // drop off location type

        assertCellEquals(sheet, 10, 5, "10/09/2020") // move date

        assertCellEquals(sheet, 10, 6, "09/09/2020") // cancellation date
        assertCellEquals(sheet, 10, 7, "22:00") // cancellation time

        assertCellEquals(sheet, 10, 8, person.prisonNumber)
        assertCellEquals(sheet, 10, 9, 10.01) // price
        assertCellEquals(sheet, 10, 10, "MoveCancel: Cancelled due to snow") // notes


    }
}