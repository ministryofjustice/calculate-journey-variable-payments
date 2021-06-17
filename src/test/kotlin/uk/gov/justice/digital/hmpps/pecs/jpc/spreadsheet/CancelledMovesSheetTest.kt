package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class CancelledMovesSheetTest {

  private val workbook: Workbook = SXSSFWorkbook()

  @Test
  internal fun `test cancelled prices`() {
    val move = moveM1(journeys = listOf(journeyJ1(state = JourneyState.cancelled)))
    val moves = listOf(move)
    val sheet = CancelledMovesSheet(workbook, PriceSheet.Header(defaultMoveDate10Sep2020, ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020), Supplier.SERCO))
    sheet.writeMoves(moves)

    assertThat(sheet.sheet.getRow(7).getCell(0).stringCellValue).isEqualTo("CANCELLED MOVES (includes prison to prison transfer moves that have been cancelled by the population management unit after 3pm on the day before the move)")

    sheet.sheet.getRow(8).apply {
      assertThat(getCell(0).stringCellValue).isEqualTo("Move ID")
      assertThat(getCell(1).stringCellValue).isEqualTo("Pick up")
      assertThat(getCell(2).stringCellValue).isEqualTo("Location Type")
      assertThat(getCell(3).stringCellValue).isEqualTo("Drop off")
      assertThat(getCell(4).stringCellValue).isEqualTo("Location Type")
      assertThat(getCell(5).stringCellValue).isEqualTo("Move date")
      assertThat(getCell(6).stringCellValue).isEqualTo("Cancellation date")
      assertThat(getCell(7).stringCellValue).isEqualTo("Cancellation time")
      assertThat(getCell(8).stringCellValue).isEqualTo("NOMIS prison ID")
      assertThat(getCell(9).stringCellValue).isEqualTo("Price")
      assertThat(getCell(10).stringCellValue).isEqualTo("Notes")
    }

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
