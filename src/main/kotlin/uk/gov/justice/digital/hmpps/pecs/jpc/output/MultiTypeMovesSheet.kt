package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice

class MultiTypeMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Multi-type")!!, header) {

    override fun writeMove(price: MovePrice) {
        writeMoveRow(price, true)
        writeJourneyRows(price.journeyPrices)
    }
}
