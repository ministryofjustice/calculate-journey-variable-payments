package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

class StandardMovesSheet(workbook: Workbook, private val date: LocalDate, supplier: Supplier) {

    private val sheet: Sheet = workbook.getSheet("Standard")!!
    private val index: AtomicInteger = AtomicInteger(10)

    init {
       addHeading(date, supplier)
    }

    private fun addHeading(date: LocalDate, supplier: Supplier) {
        sheet.getRow(0).createCell(1).setCellValue(date)
        sheet.getRow(4).createCell(1).setCellValue(supplier.reportingName().capitalize())
    }

    fun add(prices: Sequence<MovePrice>) {
        prices.forEach { addPrice(it) }
    }

    private fun addPrice(price: MovePrice) {
        sheet.createRow(index.getAndIncrement()).let { row ->
            with(price.movePersonJourneysEvents.move) {
                row.createCell(0).setCellValue(reference)
                row.createCell(1).setCellValue(fromLocation)
                row.createCell(3).setCellValue(toLocation)
            }

            price.totalInPence()?.let {
                row.createCell(11).setCellValue(it.toDouble())
            } ?: row.createCell(11).setCellValue("NOT PRESENT")
        }
    }
}
