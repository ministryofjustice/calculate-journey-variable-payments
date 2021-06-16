package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import java.util.stream.Stream

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */
internal class SupplierPricesSheet(workbook: Workbook, header: Header) : PriceSheet(
  workbook.createSheet("JPC Price book"),
  header,
  dataColumnHeadings = listOf("Pick up", "Drop off", "Unit price")
) {
  override fun writeMove(move: Move) {}

  fun writePrices(prices: List<Price>) = write(prices.stream())

  fun write(prices: Stream<Price>) {
    prices.forEach {
      val row = createRow()
      row.addCell(0, it.fromLocation.siteName)
      row.addCell(1, it.toLocation.siteName)
      row.addCell(2, it.price().pounds(), fillWhitePound)
    }
  }
}
