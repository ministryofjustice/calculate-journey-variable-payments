package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun assertOnSheetName(sheet: PriceSheet, expected: String) {
  assertThat(sheet.sheet.sheetName).isEqualTo(expected)
}

fun assertOnSubheading(sheet: PriceSheet, expected: String) {
  assertThat(sheet.getRow(7).getCell(0).stringCellValue).isEqualTo(expected)
}

fun assertOnColumnDataHeadings(sheet: PriceSheet, vararg columnHeadings: String) {
  sheet.getRow(8).apply {
    columnHeadings.forEachIndexed { cell, heading ->
      assertThat(getCell(cell).stringCellValue).isEqualTo(heading)
    }
  }

  assertThat(sheet.dataColumns.size).isEqualTo(columnHeadings.size)
}

fun assertOnRow(sheet: PriceSheet, row: Int, vararg expectedCellValues: Any) {
  expectedCellValues.forEachIndexed { i, v -> assertCellEquals(sheet, row, i, v) }
}

fun <T> assertCellEquals(sheet: PriceSheet, row: Int, col: Int, expectedVal: T?) {
  val actualValue = when (expectedVal) {
    is String -> sheet.getRow(row).getCell(col).stringCellValue
    is Double -> sheet.getRow(row).getCell(col).numericCellValue
    is BigDecimal -> sheet.getRow(row).getCell(col).numericCellValue.toBigDecimal().setScale(2)
    is Int -> sheet.getRow(row).getCell(col).numericCellValue.toInt()
    is LocalDate -> LocalDate.parse(
      sheet.getRow(row).getCell(col).stringCellValue,
      DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    )
    is LocalTime -> LocalTime.parse(
      sheet.getRow(row).getCell(col).stringCellValue,
      DateTimeFormatter.ofPattern("HH:mm"),
    )
    is MoveStatus -> MoveStatus.valueOf(sheet.getRow(row).getCell(col).stringCellValue)
    is MoveType -> MoveType.valueOf(sheet.getRow(row).getCell(col).stringCellValue)
    else -> throw RuntimeException("Must be a string, numeric value, local date, local time, move status or move type.")
  }

  assertThat(actualValue).isEqualTo(expectedVal)
}
