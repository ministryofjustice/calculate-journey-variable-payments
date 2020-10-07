package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice

class LongHaulMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Long haul")!!, header) {

    override fun writeMove(price: MovePrice) {
        writeMoveRow(price)
        writeJourneyRows(price.journeyPrices)
    }
}
