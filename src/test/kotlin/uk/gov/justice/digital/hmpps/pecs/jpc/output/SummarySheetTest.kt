package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

@SpringJUnitConfig(TestConfig::class)
internal class SummarySheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `summary prices`() {

        val movesDate = LocalDate.of(2020, 9, 10)
        val standardSummary = PriceSummary(20.0, 10, 5, 200)
        val longHaulSummary = PriceSummary(80.0, 100, 5, 400)

        val sheet = SummarySheet(workbook, PriceSheet.Header(movesDate, ClosedRangeLocalDate(movesDate, movesDate), Supplier.SERCO))
        sheet.writeSummaries(listOf(standardSummary, longHaulSummary))

        // Standard move summaries at row 9
        assertCellEquals(sheet, 9, 1, standardSummary.percentage)
        assertCellEquals(sheet, 9, 2, standardSummary.volume.toDouble())
        assertCellEquals(sheet, 9, 3, standardSummary.volumeUnpriced.toDouble())
        assertCellEquals(sheet, 9, 4, standardSummary.totalPriceInPounds)

        // Long haul summaries at row 12
        assertCellEquals(sheet, 12, 1, longHaulSummary.percentage)

        // Summary of summaries at row 25
        assertCellEquals(sheet, 28, 1, 100.0) // overall %
        assertCellEquals(sheet, 28, 2, 110.0) // overall volume
        assertCellEquals(sheet, 28, 3, 10.0) // overall volume unpriced
        assertCellEquals(sheet, 28, 4, 6.0) // overall total in £


    }
}