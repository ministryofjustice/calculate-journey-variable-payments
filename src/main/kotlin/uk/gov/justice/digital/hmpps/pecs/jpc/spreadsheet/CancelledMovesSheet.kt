package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class CancelledMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Cancelled")!!, header) {

    override fun writeMove(move: Move) = writeMoveRow(move,rowIndex.get() % 2 == 0, false)

    override fun writeMoveRow(move: Move, isShaded: Boolean, showNotes: Boolean){
        val fill = if(isShaded) fillBlue else null
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(move) {
            add(0, reference)
            add(1, fromSiteName())
            add(2, fromLocationType())
            add(3, toSiteName())
            add(4, toLocationType())
            add(5, moveDate())
            add(6, dropOffOrCancelledDate())
            add(7, dropOffOrCancelledTime())
            add(8, prisonNumber)
            if(hasPrice()) row.addCell(9, totalInPounds(), if(isShaded) fillBluePound else fillWhitePound) else add(9, "NOT PRESENT")
            add(10, notes)
        }
    }
}
