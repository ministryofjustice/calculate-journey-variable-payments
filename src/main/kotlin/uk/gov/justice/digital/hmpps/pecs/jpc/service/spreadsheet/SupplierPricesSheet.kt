package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceException
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.UNIT_PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.UNIT_PRICE_EXCEPTION
import java.util.UUID
import java.util.stream.Stream

/**
 * Sheet to represent the actual JPC supplier prices used for all of the calculations in the outputted spreadsheet.
 */

typealias PriceId = UUID

internal class SupplierPricesSheet(workbook: Workbook, private val header: Header) : PriceSheet(
  sheet = workbook.createSheet("JPC Price book"),
  header = header,
  dataColumns = listOf(PICK_UP, DROP_OFF, UNIT_PRICE, UNIT_PRICE_EXCEPTION)
) {
  override fun writeMove(move: Move) {}

  fun write(prices: Stream<Price>, possiblePriceExceptions: Map<PriceId, List<PriceException>>) {
    prices.use {

      if (possiblePriceExceptions.isEmpty()) {
        prices.forEach {
          val row = createRow()
          row.addCell(0, it.fromLocation.siteName)
          row.addCell(1, it.toLocation.siteName)
          row.addCell(2, it.price().pounds(), fillWhitePound)
        }
      } else {
        val startOfMonth = header.startOfMonth().value

        prices.forEach {
          val row = createRow()
          row.addCell(0, it.fromLocation.siteName)
          row.addCell(1, it.toLocation.siteName)
          row.addCell(2, it.price().pounds(), fillWhitePound)
          row.addCell(
            3,
            possiblePriceExceptions[it.id]?.first { pe -> pe.month == startOfMonth }?.price()?.pounds(),
            fillWhitePound
          )
        }
      }
    }
  }

  private fun Header.startOfMonth() = this.dateRange.start.month
}
