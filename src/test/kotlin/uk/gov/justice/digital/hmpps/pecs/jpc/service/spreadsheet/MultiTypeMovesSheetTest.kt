package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange

internal class MultiTypeMovesSheetTest {

  private val multiTypeMovesSheet = MultiTypeMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      DateRange(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `multi type moves`() {
    multiTypeMovesSheet.writeMoves(listOf())

    assertOnSheetName(multiTypeMovesSheet, "Multi-type")
    assertOnSubheading(multiTypeMovesSheet, "MULTI-TYPE MOVES (includes combinations of move types)")
    assertOnColumnDataHeadings(
      multiTypeMovesSheet,
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
      "Price",
      "Contractor billable?",
      "Notes"
    )
  }
}
