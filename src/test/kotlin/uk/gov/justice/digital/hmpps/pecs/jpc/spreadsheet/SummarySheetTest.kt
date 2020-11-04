package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MovesCountAndSummaries

@SpringJUnitConfig(TestConfig::class)
internal class SummarySheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `summary prices`() {

        val standardSummary = Summary(MoveType.STANDARD, 10.0, 2, 1, 200)
        val longHaulSummary = Summary(MoveType.LONG_HAUL, 50.0, 10, 10, 400)
        val summaries = MovesCountAndSummaries(1, listOf(standardSummary, longHaulSummary, Summary(), Summary(), Summary(), Summary()))

        val sheet = SummarySheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeSummaries(summaries)

        // Standard move summaries at row 9
        assertCellEquals(sheet, 9, 1, 10.0)
        assertCellEquals(sheet, 9, 2, 2.0)
        assertCellEquals(sheet, 9, 3, 1.0)
        assertCellEquals(sheet, 9, 4, 2.0)

        // Long haul summaries at row 12
        assertCellEquals(sheet, 12, 1, 50.0)

        // Summary of summaries at row 25
        assertCellEquals(sheet, 28, 1, 60.0) // overall %
        assertCellEquals(sheet, 28, 2, 12.0) // overall volume
        assertCellEquals(sheet, 28, 3, 11.0) // overall volume unpriced
        assertCellEquals(sheet, 28, 4, 6.0) // overall total in £


    }
}