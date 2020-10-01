package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Standard")!!, header) {

    override fun add(row: Row, price: MovePrice) {
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
