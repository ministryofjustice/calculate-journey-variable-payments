package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class JourneysSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.createSheet("Journeys"), header) {

  override fun writeMove(move: Move) {}

  fun writeJourneys(journeyWithPrices: List<JourneyWithPrice>) {
    journeyWithPrices.forEach {
      val row = createRow()
      row.addCell(0, it.fromSiteName())
      row.addCell(1, it.toSiteName())
      row.addCell(2, it.volume)
      row.addCell(3, it.billableJourneyCount())
      if (it.unitPriceInPence != null && it.unitPriceInPence > 0) row.addCell(4, it.unitPriceInPounds(), fillWhitePound) else row.addCell(4, "NOT PRESENT")
      row.addCell(5, it.totalPriceInPounds(), fillWhitePound)
    }
  }
}
