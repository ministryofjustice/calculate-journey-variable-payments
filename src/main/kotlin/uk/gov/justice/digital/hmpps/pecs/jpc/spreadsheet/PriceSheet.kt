package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.*
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    protected val rowIndex: AtomicInteger = AtomicInteger(10)
    protected fun createRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.createRow(rIndex)
    protected fun getRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.getRow(rIndex)

    protected val formatPound = sheet.workbook.createDataFormat().getFormat("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")
    protected val formatPercentage = sheet.workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 ))

    protected val fillGrey = sheet.workbook.createCellStyle()
    protected val fillBlue = sheet.workbook.createCellStyle()

    protected val fillGreyPound = sheet.workbook.createCellStyle()
    protected val fillBluePound = sheet.workbook.createCellStyle()
    protected val fillWhitePound = sheet.workbook.createCellStyle()

    protected val fillGreyPercentage = sheet.workbook.createCellStyle()
    protected val fillBluePercentage = sheet.workbook.createCellStyle()
    protected val fillWhitePercentage = sheet.workbook.createCellStyle()


    init {
        fillGrey.fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        fillGrey.fillPattern = FillPatternType.LEAST_DOTS

        fillGreyPound.fillForegroundColor = fillGrey.fillForegroundColor
        fillGreyPound.fillPattern = fillGrey.fillPattern
        fillGreyPound.dataFormat = formatPound

        fillGreyPercentage.fillForegroundColor = fillGrey.fillForegroundColor
        fillGreyPercentage.fillPattern = fillGrey.fillPattern
        fillGreyPercentage.dataFormat = formatPercentage

        fillBlue.fillForegroundColor = IndexedColors.BLUE.getIndex()
        fillBlue.fillPattern = FillPatternType.SPARSE_DOTS

        fillBluePound.fillForegroundColor = fillBlue.fillForegroundColor
        fillBluePound.fillPattern = fillBluePound.fillPattern
        fillBluePound.dataFormat = formatPound

        fillBluePercentage.fillForegroundColor = fillBlue.fillForegroundColor
        fillBluePercentage.fillPattern = fillBluePound.fillPattern
        fillBluePercentage.dataFormat = formatPercentage

        fillWhitePound.dataFormat = formatPound
        fillWhitePercentage.dataFormat = formatPercentage

        applyHeader()
    }


    private fun applyHeader() {
        sheet.getRow(0).getCell(1).setCellValue(header.dateRun)
        sheet.getRow(4).createCell(1).setCellValue(header.supplier.name)
        sheet.getRow(5).getCell(1).setCellValue(header.dateRange.start)
        sheet.getRow(5).getCell(3).setCellValue(header.dateRange.endInclusive)
    }


    protected open fun writeMoveRow(move: Move, isShaded: Boolean, showNotes: Boolean = true){
        val fill = if(isShaded) fillBlue else null
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(move) {
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
            if(hasPrice()) row.addCell(11, totalInPounds(), if(isShaded) fillBluePound else fillWhitePound) else add(11, "NOT PRESENT")
            add(12, "") // billable is empty for a move
            add(13, if(showNotes) notes else "")
        }
    }

    protected open fun writeJourneyRow(journeyNumber: Int, journey: Journey){
        val isShaded = journeyNumber % 2 == 1
        val fill = if(isShaded) fillGrey else null
        val row = createRow()
        fun <T>add(col: Int, value: T?) = row.addCell(col, value, fill)
        with(journey) {
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
            if(hasPrice()) row.addCell(11, priceInPounds(), if(isShaded) fillGreyPound else fillWhitePound) else add(11, "NOT PRESENT")
            add(12, isBillable())
            add(13, notes)
        }
    }

    fun writeJourneyRows(journeys: Collection<Journey>) {
        journeys.forEachIndexed { i, j ->
            writeJourneyRow(i, j)
        }
    }

    /**
     * Write the value to the cell for the given col index
     * @param col - index of the column to create the cell for this row
     * @param value - String, Double or Int value to write to the cell
     * @param cellStyle - optional CellStyle to set on the cell
     */
    protected fun <T>Row.addCell(col: Int, value: T?, cellStyle: CellStyle? = null){
        val cell = createCell(col)
        cellStyle?.let { cell.cellStyle = it }

        when(value){
            is String -> cell.setCellValue(value)
            is Double -> cell.setCellValue(value)
            is Int -> cell.setCellValue(value.toDouble())
        }
    }

    fun writeMoves(moves: List<Move>) = moves.forEach { writeMove(it) }

    /**
     * Implemented in subclasses for specific move price type
     */
    protected abstract fun writeMove(move: Move)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}

