package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

@SpringJUnitConfig(TestConfig::class)
internal class SummarySheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `summary prices`() {

        val move = moveModel(journeys = mutableListOf(journeyModel()))
        val moves = MovesAndSummary(listOf(move), Summary(10.0, 2, 1, 200))
        val longHaulMoves = MovesAndSummary(listOf(move), Summary(50.0, 10, 10, 400))
        val allMoves = MovePriceTypeWithMovesAndSummary(moves, longHaulMoves, moves, moves, moves, moves)

        val sheet = SummarySheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeSummaries(allMoves)

        // Standard move summaries at row 9
        assertCellEquals(sheet, 9, 1, 10.0)
        assertCellEquals(sheet, 9, 2, 2.0)
        assertCellEquals(sheet, 9, 3, 1.0)
        assertCellEquals(sheet, 9, 4, 2.0)

        // Long haul summaries at row 12
        assertCellEquals(sheet, 12, 1, 50.0)

        // Summary of summaries at row 25
        assertCellEquals(sheet, 28, 1, 100.0) // overall %
        assertCellEquals(sheet, 28, 2, 20.0) // overall volume
        assertCellEquals(sheet, 28, 3, 15.0) // overall volume unpriced
        assertCellEquals(sheet, 28, 4, 14.0) // overall total in £


    }
}