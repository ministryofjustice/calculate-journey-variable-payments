package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */
internal class SupplierPricesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("JPC Price book")!!, header) {
  override fun writeMove(move: Move) {}

  fun writePrices(prices: List<Price>) {
    prices.sortedBy { price -> price.fromLocation.siteName + price.toLocation.siteName }.forEach {
      val row = createRow()
      row.addCell(0, it.fromLocation.siteName)
      row.addCell(1, it.toLocation.siteName)
      row.addCell(2, it.price().pounds(), ::dataFormatPound)
    }
  }
}
