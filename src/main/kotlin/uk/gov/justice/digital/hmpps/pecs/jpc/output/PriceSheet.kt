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

    fun add(prices: Sequence<MovePrice>) {
        prices.forEach { addPrice(it) }
    }

    private fun addPrice(price: MovePrice) {
        add(sheet.createRow(index.getAndIncrement()), price)
    }

    protected abstract fun add(row: Row, price: MovePrice)

    data class Header(val dateRun: LocalDate, val dateRange: ClosedRange<LocalDate>, val supplier: Supplier)
}