package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice

class CancelledMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Cancelled")!!, header) {

    override fun writeMove(price: MovePrice) = writeMoveRow(price,rowIndex.get() % 2 == 0, false)

    override fun writeRow(row: Row, rowValue: RowValue, showNotes: Boolean, vararg styleAppliers: (cs: CellStyle) -> Unit){
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, *styleAppliers)
        with(rowValue) {
            add(0, ref)
            add(1, pickUp)
            add(2, pickUpLocationType)
            add(3, dropOff)
            add(4, dropOffLocationType)
            add(5, moveDate)
            add(6, cancelledDate)
            add(7, cancelledTime)
            add(8, prisonNumber)
            priceInPounds?.let{
                row.addCell(9, priceInPounds, *styleAppliers, ::dataFormatPound) } ?:
            add(9, "NOT PRESENT")
            add(10, notes)
        }
    }

}
