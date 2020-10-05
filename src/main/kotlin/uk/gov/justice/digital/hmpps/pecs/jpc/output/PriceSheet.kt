package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

abstract class PriceSheet(val sheet: Sheet, private val header: Header) {

    private val index: AtomicInteger = AtomicInteger(10)

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

    fun writeStandardRow(row: Row, price: MovePrice) {

        with(StandardValues.fromMovePrice(price)) {
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

            totalInPence?.let {
                row.createCell(11).setCellValue(it.toDouble())
            } ?: row.createCell(11).setCellValue("NOT PRESENT")
        }


    }

    fun Row.addCell(col: Int, value: String?) = createCell(col).setCellValue(value)

    fun addPrices(prices: Sequence<MovePrice>) {
        prices.forEach { addPrice(it) }
    }

    private fun addPrice(price: MovePrice) {
        writeRow(sheet.createRow(index.getAndIncrement()), price)
    }

    protected abstract fun writeRow(row: Row, price: MovePrice)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}