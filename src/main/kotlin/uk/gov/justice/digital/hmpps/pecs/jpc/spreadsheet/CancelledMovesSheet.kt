package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class CancelledMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Cancelled"),
  header = header,
  subheading = "CANCELLED MOVES (includes prison to prison transfer moves that have been cancelled by the population management unit after 3pm on the day before the move)",
  dataColumnHeadings = listOf(
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
) {

  override fun writeMove(move: Move) = writeMoveRow(move, false)

  override fun writeMoveRow(move: Move, showNotes: Boolean) {
    val row = createRow()
    fun <T> add(col: Int, value: T?) = row.addCell(col, value, fillShaded)
    with(move) {
      add(0, reference)
      add(1, fromSiteName())
      add(2, fromLocationType())
      add(3, toSiteName())
      add(4, toLocationType())
      add(5, moveDate())
      add(6, dropOffOrCancelledDate())
      add(7, dropOffOrCancelledTime())
      add(8, person?.prisonNumber)
      if (hasPrice()) row.addCell(9, totalInPounds(), fillShadedPound) else add(9, "NOT PRESENT")
      add(10, notes)
    }
  }
}
