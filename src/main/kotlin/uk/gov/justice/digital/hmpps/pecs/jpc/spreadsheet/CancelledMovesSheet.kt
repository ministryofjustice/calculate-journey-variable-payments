package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.CANCELLATION_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.CANCELLATION_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.LOCATION_TYPE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.MOVE_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.MOVE_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.NOMIS_PRISON_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.NOTES
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PRICE

class CancelledMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Cancelled"),
  header = header,
  subheading = "CANCELLED MOVES (includes prison to prison transfer moves that have been cancelled by the population management unit after 3pm on the day before the move)",
  dataColumns = listOf(
    MOVE_ID,
    PICK_UP,
    LOCATION_TYPE,
    DROP_OFF,
    LOCATION_TYPE,
    MOVE_DATE,
    CANCELLATION_DATE,
    CANCELLATION_TIME,
    NOMIS_PRISON_ID,
    PRICE,
    NOTES
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
