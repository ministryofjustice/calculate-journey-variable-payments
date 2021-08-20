package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

internal class CancelledMovesSheetTest {

  private val move = moveM1(journeys = listOf(journeyJ1(state = JourneyState.cancelled)))
  private val cancelledMovesSheet = CancelledMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `cancelled prices`() {
    cancelledMovesSheet.writeMoves(listOf(move))

    assertOnSheetName(cancelledMovesSheet, "Cancelled")
    assertOnSubheading(
      cancelledMovesSheet,
      "CANCELLED MOVES (includes prison to prison transfer moves that have been cancelled by the population management unit after 3pm on the day before the move)"
    )
    assertOnColumnDataHeadings(
      cancelledMovesSheet,
      "Move ID",
      "Pick up",
      "Location Type",
      "Drop off",
      "Location Type",
      "Move date",
      "Cancellation date",
      "Cancellation time",
      "NOMIS prison ID",
      "Price",
      "Notes"
    )

    assertCellEquals(cancelledMovesSheet, 9, 0, "REF1")
    assertCellEquals(cancelledMovesSheet, 9, 1, "from") // pick up sitename
    assertCellEquals(cancelledMovesSheet, 9, 2, "PR") // pick up location type
    assertCellEquals(cancelledMovesSheet, 9, 3, "to") // drop off sitename
    assertCellEquals(cancelledMovesSheet, 9, 4, "PR") // drop off location type
    assertCellEquals(cancelledMovesSheet, 9, 5, "10/09/2020") // Move date
    assertCellEquals(cancelledMovesSheet, 9, 6, "10/09/2020") // Cancellation date
    assertCellEquals(cancelledMovesSheet, 9, 7, "10:00") // Cancellation time
    assertCellEquals(cancelledMovesSheet, 9, 8, "PR101") // prison number
    assertCellEquals(cancelledMovesSheet, 9, 9, 1.0) // price
    assertCellEquals(cancelledMovesSheet, 9, 10, "some notes") // should only show the redirect event notes
  }
}
