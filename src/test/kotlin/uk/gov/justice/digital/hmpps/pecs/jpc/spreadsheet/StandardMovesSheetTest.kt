package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal class StandardMovesSheetTest {

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
    val sms = StandardMovesSheet(SXSSFWorkbook(), PriceSheet.Header(date, ClosedRangeLocalDate(date, date), supplier))

    assertOnSheetName(sms, "Standard")
    assertThat(sms.sheet.getRow(4).getCell(2).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
    assertThat(sms.sheet.getRow(4).getCell(0).stringCellValue.uppercase()).isEqualTo(supplier.name)
    assertOnSubheading(sms, "STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)")
    assertOnColumnDataHeadings(
      sms,
      "Move ID",
      "Pick up",
      "Location Type",
      "Drop off",
      "Location Type",
      "Pick up date",
      "Pick up time",
      "Drop off date",
      "Drop off time",
      "Vehicle reg",
      "NOMIS prison ID",
      "Price"
    )
  }

  @Test
  internal fun `test prices`() {
    val sms = StandardMovesSheet(
      SXSSFWorkbook(),
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
    is String -> sheet.getRow(row).getCell(col).stringCellValue
    is Double -> sheet.getRow(row).getCell(col).numericCellValue
    is Int -> sheet.getRow(row).getCell(col).numericCellValue.toInt()
    is LocalDate -> LocalDate.parse(sheet.getRow(row).getCell(col).stringCellValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    is LocalTime -> LocalTime.parse(sheet.getRow(row).getCell(col).stringCellValue, DateTimeFormatter.ofPattern("HH:mm"))
    else -> throw RuntimeException("Must be a string, numeric value, local date or local time.")
  }
  assertThat(actualValue).isEqualTo(expectedVal)
}
