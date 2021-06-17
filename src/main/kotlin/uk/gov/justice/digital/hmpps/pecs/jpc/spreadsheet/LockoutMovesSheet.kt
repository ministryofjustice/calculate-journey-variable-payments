package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class LockoutMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Lockouts"),
  header = header,
  subheading = "LOCKOUTS (refused admission to prison)",
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
    "Price",
    "Contractor billable?",
    "Notes"
  )
) {

  override fun writeMove(move: Move) {
    writeMoveRow(move)
    writeJourneyRows(move.journeys)
  }
}
