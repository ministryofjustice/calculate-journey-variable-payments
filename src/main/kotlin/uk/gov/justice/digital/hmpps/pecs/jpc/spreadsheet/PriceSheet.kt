package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

  protected val rowIndex: AtomicInteger = AtomicInteger(9)
  protected fun createRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.createRow(rIndex)
  protected fun getRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.getRow(rIndex)

  protected val formatPound = sheet.workbook.createDataFormat().getFormat("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")
  protected val formatPercentage = sheet.workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10))
  protected val formatDate = sheet.workbook.createDataFormat().getFormat("DD/MM/YYYY")
  protected val formatMonthYear = sheet.workbook.createDataFormat().getFormat("MMMM YYYY")

  protected val fontWhite: Font = sheet.workbook.createFont().apply {
    color = IndexedColors.WHITE1.index
  }

  protected val headerMonthYearStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    setFont(fontWhite)
    fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    fillPattern = FillPatternType.SOLID_FOREGROUND
    dataFormat = formatMonthYear
    alignment = HorizontalAlignment.LEFT
  }

  protected val headerExportDateStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    setFont(fontWhite)
    fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    fillPattern = FillPatternType.SOLID_FOREGROUND
    dataFormat = formatDate
    alignment = HorizontalAlignment.LEFT
  }

  protected val headerSupplierNameStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    setFont(fontWhite)
    fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    fillPattern = FillPatternType.SOLID_FOREGROUND
    alignment = HorizontalAlignment.LEFT
  }

  protected val fillGrey = sheet.workbook.createCellStyle()
  protected val fillLemon = sheet.workbook.createCellStyle()

  protected val fillGreyPound = sheet.workbook.createCellStyle()
  protected val fillLemonPound = sheet.workbook.createCellStyle()
  protected val fillWhitePound = sheet.workbook.createCellStyle()

  protected val fillGreyPercentage = sheet.workbook.createCellStyle()
  protected val fillLemonPercentage = sheet.workbook.createCellStyle()
  protected val fillWhitePercentage = sheet.workbook.createCellStyle()

  init {
    fillGrey.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    fillGrey.fillPattern = FillPatternType.LEAST_DOTS

    fillGreyPound.fillForegroundColor = fillGrey.fillForegroundColor
    fillGreyPound.fillPattern = fillGrey.fillPattern
    fillGreyPound.dataFormat = formatPound

    fillGreyPercentage.fillForegroundColor = fillGrey.fillForegroundColor
    fillGreyPercentage.fillPattern = fillGrey.fillPattern
    fillGreyPercentage.dataFormat = formatPercentage

    fillLemon.fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
    fillLemon.fillPattern = FillPatternType.SOLID_FOREGROUND

    fillLemonPound.fillForegroundColor = fillLemon.fillForegroundColor
    fillLemonPound.fillPattern = fillLemon.fillPattern
    fillLemonPound.dataFormat = formatPound

    fillLemonPercentage.fillForegroundColor = fillLemon.fillForegroundColor
    fillLemonPercentage.fillPattern = fillLemonPound.fillPattern
    fillLemonPercentage.dataFormat = formatPercentage

    fillWhitePound.dataFormat = formatPound
    fillWhitePercentage.dataFormat = formatPercentage

    applyHeader()
  }

  private fun applyHeader() {
    sheet.getRow(2).addCell(2, header.dateRange.start, headerMonthYearStyle)
    sheet.getRow(2).addCell(4, header.dateRun, headerExportDateStyle)
    sheet.getRow(4).addCell(0, header.supplier.name, headerSupplierNameStyle)
  }

  protected open fun writeMoveRow(move: Move, isShaded: Boolean, showNotes: Boolean = true) {
    val fill = if (isShaded) fillLemon else null
    val row = createRow()
    fun <T> add(col: Int, value: T?) = row.addCell(col, value, fill)
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
      add(10, person?.prisonNumber)
      if (hasPrice()) row.addCell(11, totalInPounds(), if (isShaded) fillLemonPound else fillWhitePound) else add(11, "NOT PRESENT")
      add(12, "") // billable is empty for a move
      add(13, if (showNotes) notes else "")
    }
  }

  protected open fun writeJourneyRow(journeyNumber: Int, journey: Journey) {
    val isShaded = journeyNumber % 2 == 1
    val fill = if (isShaded) fillGrey else null
    val row = createRow()
    fun <T> add(col: Int, value: T?) = row.addCell(col, value, fill)
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
      if (hasPrice()) row.addCell(11, priceInPounds(), if (isShaded) fillGreyPound else fillWhitePound) else add(11, "NOT PRESENT")
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
  protected fun <T> Row.addCell(col: Int, value: T?, cellStyle: CellStyle? = null) {
    val cell = createCell(col)
    cellStyle?.let { cell.cellStyle = it }

    when (value) {
      is String -> cell.setCellValue(value)
      is Double -> cell.setCellValue(value)
      is Int -> cell.setCellValue(value.toDouble())
      is LocalDate -> cell.setCellValue(value)
    }
  }

  fun writeMoves(moves: List<Move>) = moves.forEach { writeMove(it) }

  /**
   * Implemented in subclasses for specific move price type
   */
  protected abstract fun writeMove(move: Move)

  data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}
