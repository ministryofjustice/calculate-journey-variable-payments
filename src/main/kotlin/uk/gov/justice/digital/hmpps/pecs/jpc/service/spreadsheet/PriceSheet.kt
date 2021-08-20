package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.pdfbox.util.Hex
import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

abstract class PriceSheet(
  val sheet: Sheet,
  private val header: Header,
  private val subheading: String? = null,
  private val dataColumns: List<DataColumn> = listOf()
) {

  enum class DataColumn(val label: String, val width: Int = 4500) {
    BILLABLE_JOURNEY_COUNT("Billable journey count", 6000),
    CANCELLATION_DATE("Cancellation date"),
    CANCELLATION_TIME("Cancellation time"),
    CONTRACTOR_BILLABLE("Contractor billable?", 6000),
    DROP_OFF("Drop off", 9000),
    DROP_OFF_DATE("Drop off date"),
    DROP_OFF_TIME("Drop off time"),
    LOCATION_TYPE("Location Type"),
    MOVE_DATE("Move date"),
    MOVE_ID("Move ID"),
    MOVE_TYPE("Move type"),
    MOVE_VOLUME("Move volume"),
    MOVE_VOLUME_WITHOUT_PRICES("Move volume without prices"),
    NAME("Name"),
    NOMIS_AGENCY_ID("NOMIS Agency ID"),
    NOMIS_PRISON_ID("NOMIS prison ID"),
    NOTES("Notes", 15000),
    PERCENTAGE("Percentage"),
    PICK_UP("Pick up", 11000),
    PICK_UP_DATE("Pick up date"),
    PICK_UP_TIME("Pick up time"),
    PRICE("Price"),
    TOTAL_JOURNEY_COUNT("Total journey count", 5000),
    TOTAL_PRICE("Total price"),
    UNIT_PRICE("Unit price"),
    VEHICLE_REG("Vehicle reg")
  }

  private val rowIndex: AtomicInteger = AtomicInteger(9)
  private val formatPound = sheet.workbook.createDataFormat().getFormat("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")
  private val formatPercentage = sheet.workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(10))
  private val formatDate = sheet.workbook.createDataFormat().getFormat("DD/MM/YYYY")
  private val formatMonthYear = sheet.workbook.createDataFormat().getFormat("MMMM YYYY")
  private val colourDarkBlue = XSSFColor(Hex.decodeHex("1b75bc"), null)
  private val colourLightBlue = XSSFColor(Hex.decodeHex("c9daf8"), null)
  private val wrapTextRowHeight: Short = 500

  private val fontWhiteArialBold = sheet.workbook.createFont().apply {
    color = IndexedColors.WHITE.index
    fontName = "ARIAL"
    bold = true
  }

  private val fontBlackArial = sheet.workbook.createFont().apply {
    color = IndexedColors.BLACK.index
    fontName = "ARIAL"
  }

  protected val fontBlackArialBold = sheet.workbook.createFont().apply {
    color = IndexedColors.BLACK.index
    fontName = "ARIAL"
    bold = true
  }

  private val headerStyle: CellStyle = (sheet.workbook.createCellStyle() as XSSFCellStyle).apply {
    this.setFillForegroundColor(colourDarkBlue)
    this.fillPattern = FillPatternType.SOLID_FOREGROUND
    this.alignment = HorizontalAlignment.LEFT
    this.setFont(fontWhiteArialBold)
  }

  private val headerSupplierNameStyle: CellStyle =
    sheet.workbook.createCellStyle().apply { this.cloneStyleFrom(headerStyle) }

  private val headerMonthYearStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    this.cloneStyleFrom(headerStyle)
    this.dataFormat = formatMonthYear
  }

  private val headerExportDateStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    this.cloneStyleFrom(headerStyle)
    this.dataFormat = formatDate
  }

  private val subheadingStyle: CellStyle = (sheet.workbook.createCellStyle() as XSSFCellStyle).apply {
    this.setFillForegroundColor(colourLightBlue)
    fillPattern = FillPatternType.SOLID_FOREGROUND
    alignment = HorizontalAlignment.LEFT
    this.setFont(fontBlackArial)
  }

  private val columnHeadingStyle: CellStyle = (sheet.workbook.createCellStyle() as XSSFCellStyle).apply {
    alignment = HorizontalAlignment.LEFT
    this.setFont(fontBlackArialBold)
    wrapText = true
  }

  protected val fillShaded: CellStyle = sheet.workbook.createCellStyle().apply {
    this.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    this.fillPattern = FillPatternType.SOLID_FOREGROUND
  }

  protected val fillShadedPound: CellStyle = sheet.workbook.createCellStyle().apply {
    this.fillForegroundColor = fillShaded.fillForegroundColor
    this.fillPattern = fillShaded.fillPattern
    this.dataFormat = formatPound
  }

  protected val fillWhitePound: CellStyle = sheet.workbook.createCellStyle().apply { this.dataFormat = formatPound }

  protected val fillWhitePercentage: CellStyle =
    sheet.workbook.createCellStyle().apply { this.dataFormat = formatPercentage }

  init {
    addHeadings()
  }

  private fun addHeadings() {
    sheet.mergeCells(0, 0, 4)
    sheet.mergeCells(1, 0, 4)
    sheet.mergeCells(2, 0, 4)
    sheet.mergeCells(5, 0, 4)

    sheet.createRow(0).apply { this.addEmptyHeaderCell(0) }
    sheet.createRow(1).apply { this.addHeaderCell(0, "Calculate Journey Variable Payments (S2B)") }
    sheet.createRow(2).apply { this.addEmptyHeaderCell(0) }
    sheet.createRow(3).apply {
      this.addHeaderCell(0, "Supplier")
      this.addEmptyHeaderCell(1)
      this.addHeaderCell(2, "Total moves for")
      this.addEmptyHeaderCell(3)
      this.addHeaderCell(4, "Export date")
    }
    sheet.createRow(4).apply {
      this.addHeaderCell(0, header.supplier.name, headerSupplierNameStyle)
      this.addEmptyHeaderCell(1)
      this.addHeaderCell(2, header.dateRange.start, headerMonthYearStyle)
      this.addEmptyHeaderCell(3)
      this.addHeaderCell(4, header.dateRun, headerExportDateStyle)
    }
    sheet.createRow(5).apply { this.addEmptyHeaderCell(0) }

    if (subheading != null) {
      sheet.mergeCells(7, 0, max(4, dataColumns.size - 1))
      sheet.createRow(7).apply { this.addHeaderCell(0, subheading, subheadingStyle) }
    }

    if (dataColumns.isNotEmpty()) {
      for (column in dataColumns.indices) sheet.setColumnWidth(column, dataColumns[column].width)

      sheet.createRow(8).apply {
        height = wrapTextRowHeight
        dataColumns.forEachIndexed { col, dataColumn -> this.addHeaderCell(col, dataColumn.label, columnHeadingStyle) }
      }
    }
  }

  private fun Sheet.mergeCells(row: Int, firstCol: Int, lastCol: Int) =
    this.addMergedRegion(CellRangeAddress(row, row, firstCol, lastCol))

  protected fun Row.addHeaderCell(column: Int, label: Any, style: CellStyle = headerStyle) =
    this.addCell(column, label, style)

  protected fun Row.addEmptyHeaderCell(column: Int) = this.addCell(column, null, headerStyle)

  protected open fun writeMoveRow(move: Move, showNotes: Boolean = true) {
    val fill = fillShaded
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
      if (hasPrice()) row.addCell(11, totalInPounds(), fillShadedPound) else add(11, "NOT PRESENT")
      if (dataColumns.contains(DataColumn.CONTRACTOR_BILLABLE)) add(12, "") // billable is empty for a move
      if (dataColumns.contains(DataColumn.NOTES)) add(13, if (showNotes) notes else "")
    }
  }

  protected open fun writeJourneyRow(journeyNumber: Int, journey: Journey) {
    val row = createRow()
    fun <T> add(col: Int, value: T?) = row.addCell(col, value)
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
      if (hasPrice()) row.addCell(11, priceInPounds(), fillWhitePound) else add(11, "NOT PRESENT")
      if (dataColumns.contains(DataColumn.CONTRACTOR_BILLABLE)) add(12, isBillable())
      if (dataColumns.contains(DataColumn.NOTES)) add(13, notes)
    }
  }

  protected fun createRow(rIndex: Int = rowIndex.getAndIncrement()): Row = sheet.createRow(rIndex)

  fun writeJourneyRows(journeys: Collection<Journey>) {
    journeys.forEachIndexed { i, j ->
      writeJourneyRow(i, j)
    }
  }

  /**
   * Write the value to the cell for the given col index
   * @param col - index of the column to create the cell for this row
   * @param value - String, Double, Int or LocalDate value to write to the cell
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

  fun getRow(rowNum: Int): Row = sheet.getRow(rowNum)

  data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}
