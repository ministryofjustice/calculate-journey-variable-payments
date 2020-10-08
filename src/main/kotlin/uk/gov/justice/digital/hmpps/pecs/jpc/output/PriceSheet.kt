package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.*
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger


abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    protected val rowIndex: AtomicInteger = AtomicInteger(10)

    private fun createRow(): Row = sheet.createRow(rowIndex.getAndIncrement())

    private val greyCellStyle: CellStyle = sheet.workbook.createCellStyle()

    init {
        greyCellStyle.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        greyCellStyle.fillPattern = FillPatternType.FINE_DOTS
    }


    init {
        applyHeader()
    }

    private fun applyHeader() {
        sheet.getRow(0).getCell(1).setCellValue(header.dateRun)
        sheet.getRow(4).createCell(1).setCellValue(header.supplier.reportingName().capitalize())
        sheet.getRow(5).getCell(1).setCellValue(header.dateRange.start)
        sheet.getRow(5).getCell(3).setCellValue(header.dateRange.endInclusive)

        // TODO need to add version as well.
    }

    private fun writeRow(row: Row, rowValue: RowValue, isShaded: Boolean = false, showNotes: Boolean = true){
        with(rowValue) {
            row.addCell(0, ref, isShaded)
            row.addCell(1, pickUp, isShaded)
            row.addCell(2, pickUpLocationType, isShaded)
            row.addCell(3, dropOff, isShaded)
            row.addCell(4, dropOffLocationType, isShaded)
            row.addCell(5, pickUpDate, isShaded)
            row.addCell(6, pickUpTime, isShaded)
            row.addCell(7, dropOffDate, isShaded)
            row.addCell(8, dropOffTime, isShaded)
            row.addCell(9, vehicleReg, isShaded)
            row.addCell(10, prisonNumber, isShaded)
            row.addCell(11, priceInPounds ?: "NOT PRESENT", isShaded)
            row.addCell(12, billable, isShaded)
            row.addCell(13, if(showNotes) notes else "", isShaded)
        }
    }

    fun writeMoveRow(price: MovePrice, isShaded: Boolean, showNotes: Boolean = true) = writeRow(createRow(), RowValue.forMovePrice(price), isShaded, showNotes )

    fun writeJourneyRows(prices: List<JourneyPrice>) {
        prices.forEachIndexed { i, jp ->
            writeRow(createRow(), RowValue.forJourneyPrice(i + 1, jp))
        }

    }
    private fun <T>Row.addCell(col: Int, value: T?, isShaded: Boolean){
        val cell = createCell(col)
        if(isShaded) cell.cellStyle = greyCellStyle
        when(value){
            is String -> cell.setCellValue(value)
            is Double -> cell.setCellValue(value)
        }

    }

    fun addPrices(prices: Sequence<MovePrice>) = prices.forEach { addPrice(it) }

    private fun addPrice(price: MovePrice) = writeMove(price)

    protected abstract fun writeMove(price: MovePrice)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}
