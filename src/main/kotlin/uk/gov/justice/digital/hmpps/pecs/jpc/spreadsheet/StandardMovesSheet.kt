package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet
(
  workbook.createSheet("Standard"),
  header,
  "STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)",
  listOf(
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
) {
  override fun writeMove(move: Move) = writeMoveRow(move, false)
}
