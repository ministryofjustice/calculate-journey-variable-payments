package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel

class CancelledMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Cancelled")!!, header) {

    override fun writeMove(moveModel: MoveModel) = writeMoveRow(moveModel,rowIndex.get() % 2 == 0, false)

    override fun writeMoveRow(moveModel: MoveModel, isShaded: Boolean, showNotes: Boolean){
        val fill = if(isShaded) ::fillBlue else ::fillWhite
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(moveModel) {
            add(0, reference)
            add(1, fromSiteName())
            add(2, fromLocationType())
            add(3, toSiteName())
            add(4, toLocationType())
            add(5, moveDate())
            add(6, dropOffOrCancelledDate())
            add(7, dropOffOrCancelledTime())
            add(8, prisonNumber)
            if(hasPrice()) row.addCell(9, totalInPounds(), fill, ::dataFormatPound) else add(9, "NOT PRESENT")
            add(10, notes)
        }
    }
}
