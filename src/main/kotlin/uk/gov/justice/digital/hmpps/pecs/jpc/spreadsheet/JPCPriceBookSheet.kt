package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.*

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */
internal class JPCPriceBookSheet(workbook: Workbook) {

    private val journeyId: CellStyle = workbook.createCellStyle().apply {
        this.dataFormat = workbook.createDataFormat().getFormat("\"JID_\"00000")
    }

    private val money: CellStyle = workbook.createCellStyle().apply {
        this.dataFormat = workbook.createDataFormat().getFormat("£#,##0.00")
    }

    internal val sheet: Sheet = workbook.getSheet("JPC Price book")

    internal fun copyPricesFrom(originalPricesSheet: Sheet) {
        originalPricesSheet.copyTo(sheet)
    }

    private fun Sheet.copyTo(destination: Sheet) {
        this.forEach { sourceRow -> sourceRow.copyTo(destination.createRow(sourceRow.rowNum)) }
    }

    private fun Row.copyTo(destination: Row) {
        this.forEach { sourceCell -> sourceCell.copyTo(destination.createCell(sourceCell.columnIndex)) }
    }

    private fun Cell.copyTo(destination: Cell) {
        when (this.cellType) {
            CellType.STRING -> destination.setCellValue(this.stringCellValue)
            CellType.NUMERIC -> {
                destination.setCellValue(this.numericCellValue)
                if (this.columnIndex == 0) destination.cellStyle = journeyId else destination.cellStyle = money
            }
            else -> return
        }
    }
}
