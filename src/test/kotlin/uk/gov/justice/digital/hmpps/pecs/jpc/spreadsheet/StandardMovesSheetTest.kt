package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

internal class StandardMovesSheetTest {

  private val workbook: Workbook = SXSSFWorkbook()

  private val date: LocalDate = LocalDate.now()

  @Test
  internal fun `headings are present for Serco`() {
    assertOnHeadingsFor(date, Supplier.SERCO)
  }

  @Test
  internal fun `headings are present for Geoamey`() {
    assertOnHeadingsFor(date, Supplier.GEOAMEY)
  }

  private fun assertOnHeadingsFor(date: LocalDate, supplier: Supplier) {
    val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, ClosedRangeLocalDate(date, date), supplier))

    assertThat(sms.sheet.sheetName).isEqualTo("Standard")
    assertThat(sms.sheet.getRow(4).getCell(2).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
    assertThat(sms.sheet.getRow(4).getCell(0).stringCellValue.uppercase()).isEqualTo(supplier.name)
    assertThat(sms.sheet.getRow(7).getCell(0).stringCellValue).isEqualTo("STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)")

    sms.sheet.getRow(8).apply {
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
    }
  }

  @Test
  internal fun `test prices`() {
    val sms = StandardMovesSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )

    val move = moveM1(journeys = listOf(journeyJ1()))
    val moves = listOf(move)

    sms.writeMoves(moves)

    assertCellEquals(sms, 9, 0, "REF1")
    assertCellEquals(sms, 9, 1, "from") // pick up sitename
    assertCellEquals(sms, 9, 2, "PR") // pick up location type

    assertCellEquals(sms, 9, 3, "to") // drop off sitename
    assertCellEquals(sms, 9, 4, "PR") // drop off location type

    assertCellEquals(sms, 9, 5, "10/09/2020") // Pick up date
    assertCellEquals(sms, 9, 6, "00:00") // Pick up time
    assertCellEquals(sms, 9, 7, "10/09/2020") // Drop off date
    assertCellEquals(sms, 9, 8, "10:00") // Drop off time

    assertCellEquals(sms, 9, 9, "reg100") // vehicle reg

    assertCellEquals(sms, 9, 10, "PR101") // prison number

    assertCellEquals(sms, 9, 11, 1.0) // price

    assertCellEquals(sms, 9, 12, "") // billable shouldn't be shown
    assertCellEquals(sms, 9, 13, "") // notes shouldn't be shown for a standard move
  }
}

fun <T> assertCellEquals(sheet: PriceSheet, row: Int, col: Int, expectedVal: T?) {
  val actualValue = when (expectedVal) {
    is String -> sheet.sheet.getRow(row).getCell(col).stringCellValue
    is Double -> sheet.sheet.getRow(row).getCell(col).numericCellValue
    is Int -> sheet.sheet.getRow(row).getCell(col).numericCellValue.toInt()
    else -> throw RuntimeException("Must be a string or numeric value")
  }
  assertThat(actualValue).isEqualTo(expectedVal)
}
