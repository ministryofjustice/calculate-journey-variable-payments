package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class LongHaulMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.createSheet("Long haul"), header) {

  override fun writeMove(move: Move) {
    writeMoveRow(move)
    writeJourneyRows(move.journeys)
  }
}
