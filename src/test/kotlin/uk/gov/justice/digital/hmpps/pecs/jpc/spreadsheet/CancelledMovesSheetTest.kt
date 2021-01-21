package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class CancelledMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

  private val workbook: Workbook = XSSFWorkbook(template.get())

  @Test
  internal fun `test cancelled prices`() {
    val move = moveM1(journeys = listOf(journeyJ1(state = JourneyState.cancelled)))
    val moves = listOf(move)
    val sheet = CancelledMovesSheet(workbook, PriceSheet.Header(defaultMoveDate10Sep2020, ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020), Supplier.SERCO))
    sheet.writeMoves(moves)

    assertCellEquals(sheet, 9, 0, "REF1")

    assertCellEquals(sheet, 9, 1, "from") // pick up sitename
    assertCellEquals(sheet, 9, 2, "PR") // pick up location type
    assertCellEquals(sheet, 9, 3, "to") // drop off sitename
    assertCellEquals(sheet, 9, 4, "PR") // drop off location type

    assertCellEquals(sheet, 9, 5, "10/09/2020") // Move date
    assertCellEquals(sheet, 9, 6, "10/09/2020") // Cancellation date
    assertCellEquals(sheet, 9, 7, "10:00") // Cancellation time

    assertCellEquals(sheet, 9, 8, "PR101") // prison number
    assertCellEquals(sheet, 9, 9, 1.0) // price
    assertCellEquals(sheet, 9, 10, "some notes") // should only show the redirect event notes
  }
}
