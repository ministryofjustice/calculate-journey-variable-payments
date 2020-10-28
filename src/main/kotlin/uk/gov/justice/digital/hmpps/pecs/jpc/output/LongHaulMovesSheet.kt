package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel

class LongHaulMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Long haul")!!, header) {

    override fun writeMove(moveModel: MoveModel) {
        writeMoveRow(moveModel, true)
        writeJourneyRows(moveModel.journeys)
    }
}
