package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class RedirectionMovesSheetTest {

  private val workbook: Workbook = SXSSFWorkbook()

  @Test
  internal fun `test redirection prices`() {

    val journey1 = journeyJ1()
    val journey2 = journeyJ1(journeyId = "J2")
    val move = moveM1(journeys = listOf(journey1, journey2))
    val moves = listOf(move)

    val sheet = RedirectionMovesSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    sheet.writeMoves(moves)

    assertThat(sheet.sheet.getRow(7).getCell(0).stringCellValue).isEqualTo("REDIRECTIONS (a redirection after the move has started)")

    sheet.sheet.getRow(8).apply {
      assertThat(this.getCell(0).stringCellValue).isEqualTo("Move ID")
      assertThat(this.getCell(1).stringCellValue).isEqualTo("Pick up")
      assertThat(this.getCell(2).stringCellValue).isEqualTo("Location Type")
      assertThat(this.getCell(3).stringCellValue).isEqualTo("Drop off")
      assertThat(this.getCell(4).stringCellValue).isEqualTo("Location Type")
      assertThat(this.getCell(5).stringCellValue).isEqualTo("Pick up date")
      assertThat(this.getCell(6).stringCellValue).isEqualTo("Pick up time")
      assertThat(this.getCell(7).stringCellValue).isEqualTo("Drop off date")
      assertThat(this.getCell(8).stringCellValue).isEqualTo("Drop off time")
      assertThat(this.getCell(9).stringCellValue).isEqualTo("Vehicle reg")
      assertThat(this.getCell(10).stringCellValue).isEqualTo("NOMIS prison ID")
      assertThat(this.getCell(11).stringCellValue).isEqualTo("Price")
      assertThat(this.getCell(12).stringCellValue).isEqualTo("Contractor billable?")
      assertThat(this.getCell(13).stringCellValue).isEqualTo("Notes (reason codes or supplier notes)")
    }

    assertCellEquals(sheet, 9, 0, "REF1")

    assertCellEquals(sheet, 9, 1, "from") // pick up site name
    assertCellEquals(sheet, 9, 2, "PR") // pick up location type
    assertCellEquals(sheet, 9, 3, "to") // drop off site name
    assertCellEquals(sheet, 9, 4, "PR") // drop off location type

    assertCellEquals(sheet, 9, 5, "10/09/2020") // Pick up date
    assertCellEquals(sheet, 9, 6, "00:00") // Pick up time
    assertCellEquals(sheet, 9, 7, "10/09/2020") // Drop off date
    assertCellEquals(sheet, 9, 8, "10:00") // Drop off time

    assertCellEquals(sheet, 9, 9, "reg100") // vehicle reg
    assertCellEquals(sheet, 9, 10, "PR101") // prison number
    assertCellEquals(sheet, 9, 11, 2.0) // price
    assertCellEquals(sheet, 9, 12, "") // billable shouldn't be shown
    assertCellEquals(sheet, 9, 13, "some notes") // should only show the redirect event notes

    // Journey 1
    assertCellEquals(sheet, 10, 0, "Journey 1")

    assertCellEquals(sheet, 10, 1, "from")
    assertCellEquals(sheet, 10, 2, "PR")
    assertCellEquals(sheet, 10, 3, "to")
    assertCellEquals(sheet, 10, 4, "PR")

    assertCellEquals(sheet, 10, 5, "10/09/2020") // Pick up date
    assertCellEquals(sheet, 10, 6, "00:00") // Pick up time
    assertCellEquals(sheet, 10, 7, "10/09/2020") // Drop off date
    assertCellEquals(sheet, 10, 8, "10:00") // Drop off time

    assertCellEquals(sheet, 10, 9, "REG200") // vehicle reg
    assertCellEquals(sheet, 10, 10, "") // no prison number for journeys
    assertCellEquals(sheet, 10, 11, 1.0) // price
    assertCellEquals(sheet, 10, 12, "YES") // contractor billable
    assertCellEquals(sheet, 10, 13, "some notes")

    // Journey 2
    assertCellEquals(sheet, 11, 0, "Journey 2")
  }
}
