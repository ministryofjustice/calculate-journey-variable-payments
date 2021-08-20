package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.UNIT_PRICE
import java.util.stream.Stream

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */
internal class SupplierPricesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("JPC Price book"),
  header = header,
  dataColumns = listOf(PICK_UP, DROP_OFF, UNIT_PRICE)
) {
  override fun writeMove(move: Move) {}

  fun write(prices: Stream<Price>) {
    prices.forEach {
      val row = createRow()
      row.addCell(0, it.fromLocation.siteName)
      row.addCell(1, it.toLocation.siteName)
      row.addCell(2, it.price().pounds(), fillWhitePound)
    }
  }
}
