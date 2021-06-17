package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet
(
  sheet = workbook.createSheet("Standard"),
  header = header,
  subheading = "STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)",
  dataColumnHeadings = listOf(
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
