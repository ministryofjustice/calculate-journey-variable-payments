package uk.gov.justice.digital.hmpps.pecs.jpc.output

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
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.io.File
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class StandardMovesSheetTest(@Autowired @Qualifier(value = "spreadsheet-template") template: File) {

    private val workbook: Workbook = XSSFWorkbook(template)

    @Test
    internal fun `fails to instantiate if expected sheet is missing`() {
        assertThatThrownBy { StandardMovesSheet(XSSFWorkbook(), PriceSheet.Header(LocalDate.now(), PriceSheet.DateRange(LocalDate.now(), LocalDate.now()), Supplier.SERCO)) }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    internal fun `default headings are applied for Serco`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(LocalDate.now(), PriceSheet.DateRange(LocalDate.now(), LocalDate.now()), Supplier.SERCO))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(LocalDate.of(2020, 10, 1))
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.SERCO.name)
    }

    @Test
    internal fun `default headings are applied for Geoamey`() {
        val sms = StandardMovesSheet(workbook, PriceSheet.Header(LocalDate.now(), PriceSheet.DateRange(LocalDate.now(), LocalDate.now()), Supplier.GEOAMEY))

        assertThat(sms.sheet.getRow(0).getCell(1).localDateTimeCellValue.toLocalDate()).isEqualTo(LocalDate.of(2020, 10, 1))
        assertThat(sms.sheet.getRow(4).getCell(1).stringCellValue.toUpperCase()).isEqualTo(Supplier.GEOAMEY.name)
    }

    // TODO still need to populate and test additional standard fields e.g. from and to, version..
    // TODO test moves prices are applied
}