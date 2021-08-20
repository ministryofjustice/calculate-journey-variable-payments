package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.assertj.core.api.Assertions.assertThat

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
}
