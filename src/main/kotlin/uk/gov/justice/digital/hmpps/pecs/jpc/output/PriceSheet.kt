package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.*
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyModel
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovesAndSummary
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger


abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    private val formatPound = sheet.workbook.createDataFormat().getFormat("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")

    private val formatPercentage = sheet.workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 ))

    protected val rowIndex: AtomicInteger = AtomicInteger(10)

    protected fun createRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.createRow(rIndex)

    protected fun getRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.getRow(rIndex)

    protected fun fillBlue(cellStyle: CellStyle){
        cellStyle.fillForegroundColor = IndexedColors.BLUE.getIndex()
        cellStyle.fillPattern = FillPatternType.SPARSE_DOTS
    }

    protected fun fillGrey(cellStyle: CellStyle){
        cellStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        cellStyle.fillPattern = FillPatternType.LEAST_DOTS
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
    protected open fun writeMoveRow(moveModel: MoveModel, isShaded: Boolean, showNotes: Boolean = true){
        val fill = if(isShaded) ::fillBlue else ::fillWhite
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(moveModel) {
            add(0, reference)
            add(1, fromSiteName())
            add(2, fromLocationType())
            add(3, toSiteName())
            add(4, toLocationType())
            add(5, pickUpDate())
            add(6, pickUpTime())
            add(7, dropOffOrCancelledDate())
            add(8, dropOffOrCancelledTime())
            add(9, vehicleRegistration)
            add(10, prisonNumber)
            totalInPounds()?.let{
                row.addCell(11, totalInPounds(), fill, ::dataFormatPound) } ?:
                add(11, "NOT PRESENT")
            add(12, "") // billable is empty for a move
            add(13, if(showNotes) notes else "")
        }
    }

    protected open fun writeJourneyRow(journeyNumber: Int, journeyModel: JourneyModel){
        val fill = if(journeyNumber % 2 == 1) ::fillGrey else ::fillWhite
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(journeyModel) {
            add(0, "Journey ${journeyNumber + 1}")
            add(1, fromSiteName())
            add(2, fromLocationType())
            add(3, toSiteName())
            add(4, toLocationType())
            add(5, pickUpDate())
            add(6, pickUpTime())
            add(7, dropOffDate())
            add(8, dropOffOrTime())
            add(9, vehicleRegistration)
            add(10, "") // prison number is empty for a journey

            priceInPounds()?.let{
                row.addCell(11, priceInPounds(), fill, ::dataFormatPound) } ?:
            add(11, "NOT PRESENT")
            add(12, isBillable())
            add(13, notes)
        }
    }


    fun writeJourneyRows(journeys: List<JourneyModel>) {
        journeys.forEachIndexed { i, j ->
            writeJourneyRow(i, j)
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

    fun writeMoves(movesAndSummary: MovesAndSummary) = movesAndSummary.moves.forEach { writeMove(it) }

    /**
     * Implemented in subclasses for specific move price type
     */
    protected abstract fun writeMove(moveModel: MoveModel)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)

}
