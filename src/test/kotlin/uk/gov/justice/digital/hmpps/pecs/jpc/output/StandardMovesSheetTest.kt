package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JCPTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

@SpringJUnitConfig(TestConfig::class)
internal class StandardMovesSheetTest(@Autowired private val template: JCPTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    private val date: LocalDate = LocalDate.now()

    @Test
    internal fun `fails to instantiate if expected sheet is missing`() {
        assertThatThrownBy { StandardMovesSheet(XSSFWorkbook(), PriceSheet.Header(LocalDate.now(), ClosedRangeLocalDate(LocalDate.now(), LocalDate.now()), Supplier.SERCO)) }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    internal fun `default headings are applied for Serco`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, ClosedRangeLocalDate(date, date), Supplier.SERCO))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.SERCO.name)
    }

    @Test
    internal fun `default headings are applied for Geoamey`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, ClosedRangeLocalDate(date, date), Supplier.GEOAMEY))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.GEOAMEY.name)
    }


    @Test
    internal fun `test prices`() {
        val movesDate = LocalDate.of(2020, 9, 10)

        val journeyWithEvents = JourneyWithEvents(journeyFactory(billable = true), listOf())
        val standardMove = Report(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = movesDate.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, notes = "completed", occurredAt = movesDate.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(journeyWithEvents)
        )

        val fromLocation = fromLocationFactory()
        val toLocation = toLocationFactory()
        val standardPrice = MovePrice(standardMove, listOf(JourneyPrice(journeyWithEvents, 1001)))

        val sms = StandardMovesSheet(workbook, PriceSheet.Header(movesDate, ClosedRangeLocalDate(movesDate, movesDate), Supplier.SERCO))
        sms.writeMoves(listOf(standardPrice))

        assertCellEquals(sms, 10, 0, standardMove.move.reference)
        assertCellEquals(sms, 10, 1, fromLocation.siteName)
        assertCellEquals(sms, 10, 2, fromLocation.locationType.name) // pick up location type

        assertCellEquals(sms, 10, 3, toLocation.siteName)
        assertCellEquals(sms, 10, 4, toLocation.locationType.name) // drop off location type

        assertCellEquals(sms, 10, 5, "10/09/2020") // Pick up date
        assertCellEquals(sms, 10, 6, "05:00") // Pick up time
        assertCellEquals(sms, 10, 7, "10/09/2020") // Drop off date
        assertCellEquals(sms, 10, 8, "10:00") // Drop off time

        assertCellEquals(sms, 10, 9, journeyWithEvents.journey.vehicleRegistration)

        assertCellEquals(sms, 10, 10, standardMove.person?.prisonNumber)

        assertCellEquals(sms, 10, 11, 10.01) // price

        assertCellEquals(sms, 10, 12, "") // billable shouldn't be shown
        assertCellEquals(sms, 10, 13, "") // notes shouldn't be shown
    }
}

fun <T>assertCellEquals(sheet: PriceSheet, row: Int, col: Int, expectedVal: T?) {
    val actualValue = when(expectedVal){
        is String -> sheet.sheet.getRow(row).getCell(col).stringCellValue
        is Double -> sheet.sheet.getRow(row).getCell(col).numericCellValue
        is Int -> sheet.sheet.getRow(row).getCell(col).numericCellValue
        else -> throw RuntimeException("Must be a string or numeric value")
    }
    assertThat(actualValue).isEqualTo(expectedVal)
}