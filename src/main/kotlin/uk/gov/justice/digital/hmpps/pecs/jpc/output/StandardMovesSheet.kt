package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Standard")!!, header) {

    override fun writeMove(moveModel: MoveModel) = writeMoveRow(moveModel,rowIndex.get() % 2 == 0, false)
}
