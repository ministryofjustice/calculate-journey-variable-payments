package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Standard")!!, header) {

  override fun writeMove(move: Move) = writeMoveRow(move, rowIndex.get() % 2 == 0, false)
}
