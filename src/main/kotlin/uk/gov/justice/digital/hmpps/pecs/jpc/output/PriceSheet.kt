package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    private val index: AtomicInteger = AtomicInteger(10)

    private fun createRow(): Row = sheet.createRow(index.getAndIncrement())

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

    private fun writeRow(row: Row, rowValue: RowValue){
        with(rowValue) {
            row.addCell(0, ref)
            row.addCell(1, pickUp)
            row.addCell(2, pickUpLocationType)
            row.addCell(3, dropOff)
            row.addCell(4, dropOffLocationType)
            row.addCell(5, pickUpDate)
            row.addCell(6, pickUpTime)
            row.addCell(7, dropOffDate)
            row.addCell(8, dropOffTime)
            row.addCell(9, vehicleReg)
            row.addCell(10, prisonNumber)

            priceInPence?.let {
                row.createCell(11).setCellValue(it.toDouble() / 100)
            } ?: row.createCell(11).setCellValue("NOT PRESENT")

            row.addCell(12, billable)
        }
    }

    fun writeMoveRow(price: MovePrice) = writeRow(createRow(), RowValue.forMovePrice(price))

    fun writeJourneyRows(prices: List<JourneyPrice>) {
        prices.forEachIndexed {i, jp ->
            writeRow(createRow(), RowValue.forJourneyPrice(i +1, jp))
        }

    }

    private fun Row.addCell(col: Int, value: String?) = createCell(col).setCellValue(value)

    fun addPrices(prices: Sequence<MovePrice>) = prices.forEach { addPrice(it) }

    private fun addPrice(price: MovePrice) = writeMove(price)

    protected abstract fun writeMove(price: MovePrice)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}
