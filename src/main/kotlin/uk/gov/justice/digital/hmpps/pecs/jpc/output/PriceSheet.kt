package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.*
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger


abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    private val formatPound = sheet.workbook.createDataFormat().getFormat("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")

    private val formatPercentage = sheet.workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 ))

    protected val rowIndex: AtomicInteger = AtomicInteger(10)

    protected fun createRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.createRow(rIndex)

    protected fun getRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.getRow(rIndex)

    protected fun fillBlue(cellStyle: CellStyle){
        cellStyle.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        cellStyle.fillPattern = FillPatternType.FINE_DOTS
    }

    protected fun fillWhite(cellStyle: CellStyle){  } // do nothing

    protected fun dataFormatPercentage(cellStyle: CellStyle){
        cellStyle.dataFormat = formatPercentage
    }

    protected fun dataFormatPound(cellStyle: CellStyle){
        cellStyle.dataFormat = formatPound
    }


    init {
        applyHeader()
    }

    private fun applyHeader() {
        sheet.getRow(0).getCell(1).setCellValue(header.dateRun)
        sheet.getRow(4).createCell(1).setCellValue(header.supplier.reportingName().capitalize())
        sheet.getRow(5).getCell(1).setCellValue(header.dateRange.start)
        sheet.getRow(5).getCell(3).setCellValue(header.dateRange.endInclusive)
    }

    /**
     * Write the Row using the RowValue value object
     * @param showNotes - Boolean indicating whether the notes column should be displayed
     * @param styleAppliers - vararg of style functions to apply to each cell
     */
    private fun writeRow(row: Row, rowValue: RowValue, showNotes: Boolean = true, vararg styleAppliers: (cs: CellStyle) -> Unit){
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, *styleAppliers)
        with(rowValue) {
            add(0, ref)
            add(1, pickUp)
            add(2, pickUpLocationType)
            add(3, dropOff)
            add(4, dropOffLocationType)
            add(5, pickUpDate)
            add(6, pickUpTime)
            add(7, dropOffDate)
            add(8, dropOffTime)
            add(9, vehicleReg)
            add(10, prisonNumber)
            priceInPounds?.let{
                row.addCell(11, priceInPounds, *styleAppliers, ::dataFormatPound) } ?:
                add(11, "NOT PRESENT")
            add(12, billable)
            add(13, if(showNotes) notes else "")
        }
    }

    fun writeMoveRow(price: MovePrice, isShaded: Boolean, showNotes: Boolean = true) {
        val fill = if(isShaded) ::fillBlue else ::fillWhite
        writeRow(createRow(), RowValue.forMovePrice(price), showNotes, fill )
    }

    fun writeJourneyRows(prices: List<JourneyPrice>) {
        prices.forEachIndexed { i, jp ->
            writeRow(createRow(), RowValue.forJourneyPrice(i + 1, jp))
        }
    }

    /**
     * Write the value to the cell for the given col index
     * @param col - index of the column to create the cell for this row
     * @param value - String, Double or Int value to write to the cell
     * @param styleAppliers - vararg of style functions to apply to the cell
     */
    protected fun <T>Row.addCell(col: Int, value: T?, vararg styleAppliers: (cs: CellStyle) -> Unit){
        val cellStyle = sheet.workbook.createCellStyle()
        styleAppliers.forEach { it(cellStyle) }
        val cell = createCell(col)
        cell.cellStyle = cellStyle

        when(value){
            is String -> cell.setCellValue(value)
            is Double -> cell.setCellValue(value)
            is Int -> cell.setCellValue(value.toDouble())
        }
    }

    fun writeMoves(prices: List<MovePrice>) = prices.forEach { writeMove(it) }

    /**
     * Implemented in subclasses for specific move price type
     */
    protected abstract fun writeMove(price: MovePrice)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}
