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


@SpringJUnitConfig(TestConfig::class)
internal class RedirectionMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `test redirection prices`() {

        val journey1 = journeyModel()
        val journey2 = journeyModel()
        val move = moveModel(journeys = mutableListOf(journey1, journey2))
        val moves = MovesAndSummary(listOf(move), Summary())
        
        val sheet = RedirectionMovesSheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeMoves(moves)

        assertCellEquals(sheet, 10, 0, "REF1")

        assertCellEquals(sheet, 10, 1, "from") // pick up sitename
        assertCellEquals(sheet, 10, 2, "PR") // pick up location type
        assertCellEquals(sheet, 10, 3, "to") // drop off sitename
        assertCellEquals(sheet, 10, 4, "PR") // drop off location type

        assertCellEquals(sheet, 10, 5, "10/09/2020") // Pick up date
        assertCellEquals(sheet, 10, 6, "00:00") // Pick up time
        assertCellEquals(sheet, 10, 7, "10/09/2020") // Drop off date
        assertCellEquals(sheet, 10, 8, "10:00") // Drop off time

        assertCellEquals(sheet, 10, 9, "reg100") // vehicle reg
        assertCellEquals(sheet, 10, 10, "PR101") // prison number
        assertCellEquals(sheet, 10, 11, 2.0) // price
        assertCellEquals(sheet, 10, 12, "") // billable shouldn't be shown
        assertCellEquals(sheet, 10, 13, "some notes") // should only show the redirect event notes

        // Journey 1
        assertCellEquals(sheet, 11, 0, "Journey 1")

        assertCellEquals(sheet, 11, 1, "from")
        assertCellEquals(sheet, 11, 2, "PR")
        assertCellEquals(sheet, 11, 3, "to")
        assertCellEquals(sheet, 11, 4, "PR")

        assertCellEquals(sheet, 11, 5, "10/09/2020") // Pick up date
        assertCellEquals(sheet, 11, 6, "00:00") // Pick up time
        assertCellEquals(sheet, 11, 7, "10/09/2020") // Drop off date
        assertCellEquals(sheet, 11, 8, "10:00") // Drop off time

        assertCellEquals(sheet, 11, 9, "REG200") // vehicle reg
        assertCellEquals(sheet, 11, 10, "") // no prison number for journeys
        assertCellEquals(sheet, 11, 11, 1.0) // price
        assertCellEquals(sheet, 11, 12, "YES") // contractor billable
        assertCellEquals(sheet, 11, 13, "some notes")

        // Journey 2
        assertCellEquals(sheet, 12, 0, "Journey 2")
    }
}