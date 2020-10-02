package uk.gov.justice.digital.hmpps.pecs.jpc.output

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculatorFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.io.File
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class StandardMovesSheetTest(@Autowired @Qualifier(value = "spreadsheet-template") template: File) {

    private val workbook: Workbook = XSSFWorkbook(template)

    private val date: LocalDate = LocalDate.now()

    @Test
    internal fun `fails to instantiate if expected sheet is missing`() {
        assertThatThrownBy { StandardMovesSheet(XSSFWorkbook(), PriceSheet.Header(LocalDate.now(), PriceSheet.DateRange(LocalDate.now(), LocalDate.now()), Supplier.SERCO)) }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    internal fun `default headings are applied for Serco`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, PriceSheet.DateRange(date, date), Supplier.SERCO))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.SERCO.name)
    }

    @Test
    internal fun `default headings are applied for Geoamey`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, PriceSheet.DateRange(date, date), Supplier.GEOAMEY))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.GEOAMEY.name)
    }


    @Test
    internal fun `test prices`(){
        val movesDate = LocalDate.of(2020, 9, 10)

        val journeyWithEvents = JourneyWithEvents(journeyFactory(billable = true), listOf())
        val standardMove = MovePersonJourneysEvents(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = movesDate.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = movesDate.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(journeyWithEvents)
        )

        val standardPrice = MovePrice(standardMove, listOf(JourneyPrice(journeyWithEvents, 1001)))

        val sms = StandardMovesSheet(workbook, PriceSheet.Header(movesDate, PriceSheet.DateRange(movesDate, movesDate), Supplier.SERCO))
        sms.add(listOf(standardPrice).asSequence())

        assertCellEquals(sms, 10, 0, standardMove.move.reference) // Move ref
        assertCellEquals(sms, 10, 5, "10/09/2020") // Pick up date

    }

    fun assertCellEquals(sms: StandardMovesSheet, row: Int, col: Int, expectedVal: String) =
            assertThat(sms.sheet.getRow(row).getCell(col).stringCellValue.toUpperCase()).isEqualTo(expectedVal)
}