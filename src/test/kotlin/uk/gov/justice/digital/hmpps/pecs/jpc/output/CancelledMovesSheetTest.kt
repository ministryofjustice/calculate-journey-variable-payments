package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class CancelledMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `test cancelled prices`() {
        val move = moveModel(journeys = mutableListOf(journeyModel(state = JourneyState.CANCELLED)))
        val moves = MovesAndSummary(listOf(move), Summary())

        val sheet = CancelledMovesSheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeMoves(moves)

        assertCellEquals(sheet, 10, 0, "REF1")

        assertCellEquals(sheet, 10, 1, "from") // pick up sitename
        assertCellEquals(sheet, 10, 2, "PR") // pick up location type
        assertCellEquals(sheet, 10, 3, "to") // drop off sitename
        assertCellEquals(sheet, 10, 4, "PR") // drop off location type

        assertCellEquals(sheet, 10, 5, "10/09/2020") // Move date
        assertCellEquals(sheet, 10, 6, "10/09/2020") // Cancellation date
        assertCellEquals(sheet, 10, 7, "10:00") // Cancellation time

        assertCellEquals(sheet, 10, 8, "PR101") // prison number
        assertCellEquals(sheet, 10, 9, 1.0) // price
        assertCellEquals(sheet, 10, 10, "some notes") // should only show the redirect event notes
    }
}