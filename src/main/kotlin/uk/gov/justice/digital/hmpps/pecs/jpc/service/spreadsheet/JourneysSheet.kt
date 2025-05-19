package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.BILLABLE_JOURNEY_COUNT
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.TOTAL_JOURNEY_COUNT
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.TOTAL_PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.UNIT_PRICE

class JourneysSheet(workbook: Workbook, header: Header) :
  PriceSheet(
    sheet = workbook.createSheet("Journeys"),
    header = header,
    subheading = "TOTAL VOLUME BY JOURNEY",
    dataColumns = listOf(PICK_UP, DROP_OFF, TOTAL_JOURNEY_COUNT, BILLABLE_JOURNEY_COUNT, UNIT_PRICE, TOTAL_PRICE),
  ) {

  override fun writeMove(move: Move) {}

  fun writeJourneys(journeyWithPrices: List<JourneyWithPrice>) {
    journeyWithPrices.forEach {
      val row = createRow()
      row.addCell(0, it.fromSiteName())
      row.addCell(1, it.toSiteName())
      row.addCell(2, it.volume)
      row.addCell(3, it.billableJourneyCount())
      if (it.unitPriceInPence != null && it.unitPriceInPence > 0) {
        row.addCell(
          4,
          it.unitPriceInPounds(),
          fillWhitePound,
        )
      } else {
        row.addCell(4, "NOT PRESENT")
      }
      row.addCell(5, it.totalPriceInPounds(), fillWhitePound)
    }
  }
}
