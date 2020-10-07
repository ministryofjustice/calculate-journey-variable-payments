package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */
internal class JPCPriceBookSheet(workbook: Workbook) {

    internal val sheet: Sheet = workbook.getSheet("JPC Price book")!!

    internal fun copyPricesFrom(originalPricesSheet: Sheet) {
        originalPricesSheet.copyTo(sheet)
    }

    private infix fun Sheet.copyTo(destination: Sheet) {
        this.forEach { sourceRow -> sourceRow.copyTo(destination.createRow(sourceRow.rowNum)) }
    }

    private infix fun Row.copyTo(destination: Row) {
        this.forEach { sourceCell -> sourceCell.copyTo(destination.createCell(sourceCell.columnIndex)) }
    }

    private infix fun Cell.copyTo(destination: Cell) {
        when (this.cellType) {
            CellType.STRING -> destination.setCellValue(this.stringCellValue)
            CellType.NUMERIC -> destination.setCellValue(this.numericCellValue)
            else -> return
        }
    }
}
