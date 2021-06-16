package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class RedirectionMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  workbook.createSheet("Redirections"),
  header,
  "REDIRECTIONS (a redirection after the move has started)",
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
    "Price",
    "Contractor billable?",
    "Notes (reason codes or supplier notes)"
  )
) {

  override fun writeMove(move: Move) {
    writeMoveRow(move)
    writeJourneyRows(move.journeys)
  }
}
