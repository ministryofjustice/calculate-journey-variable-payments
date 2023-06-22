package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move

/**
 * The moves included in this sheet for the month may be billable i.e. reconcilable, but are not actually included as
 * billable moves for some reason e.g. missing data, move type and/or the drop off time.
 */
class ReconciliationMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Reconciliation Moves"),
  header = header,
  dataColumns = listOf(
    DataColumn.MOVE_ID,
    DataColumn.MOVE_DATE,
    DataColumn.MOVE_TYPE,
    DataColumn.MOVE_STATUS,
    DataColumn.PICK_UP_DATE,
    DataColumn.PICK_UP_TIME,
    DataColumn.DROP_OFF_DATE,
    DataColumn.DROP_OFF_TIME,
  ),
) {
  override fun writeMove(move: Move) {
    createRow().apply { move.values().forEachIndexed { i, v -> addCell(i, v) } }
  }

  private fun Move.values() = arrayOf<Any?>(reference, moveDate(), moveType, status, pickUpDate(), pickUpTime(), dropOffOrCancelledDate(), dropOffOrCancelledTime())
}
