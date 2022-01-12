package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.io.Closeable

private const val FROM_LOCATION = 1
private const val TO_LOCATION = 2
private const val PRICE = 3
private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 1

/**
 * Simple wrapper class to encapsulate the logic around access to data in the supplier prices spreadsheet. When finished with the spreadsheet should be closed.
 */
class PricesSpreadsheet(
  private val spreadsheet: Workbook,
  val supplier: Supplier,
  supplierLocations: List<Location>,
  private val pricesRepository: PriceRepository,
  private val effectiveYear: Int
) : Closeable {

  val errors: MutableList<PricesSpreadsheetError> = mutableListOf()

  private val locations = supplierLocations.associateBy { it.siteName.uppercase() }

  fun forEachRow(f: (price: Price) -> Unit) {
    getRows().forEach { row -> Result.runCatching { f(mapToPrice(row)) }.onFailure { this.addError(row, it) } }
  }

  /**
   * Only rows containing prices are returned. The heading row is not included.
   */
  private fun getRows(): List<Row> =
    spreadsheet.getSheetAt(0).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

  fun mapToPrice(row: Row) = getPrice(supplier, row)

  private fun getPrice(supplier: Supplier, row: Row): Price {
    val fromLocationName =
      row.getFormattedStringCell(FROM_LOCATION) ?: throw RuntimeException("From location name cannot be blank")

    val toLocationName =
      row.getFormattedStringCell(TO_LOCATION) ?: throw RuntimeException("To location name cannot be blank")

    val price = Result.runCatching { Money.valueOf(row.getCell(PRICE).numericCellValue.toBigDecimal()).pence }
      .onSuccess { if (it == 0) throw RuntimeException("Price must be greater than zero") }
      .getOrElse { throw RuntimeException("Error retrieving price for supplier '$supplier'", it) }

    val fromLocation = locations[fromLocationName]
      ?: throw RuntimeException("From location '$fromLocationName' for supplier '$supplier' not found")

    val toLocation = locations[toLocationName]
      ?: throw RuntimeException("To location '$toLocationName' for supplier '$supplier' not found")

    failIfPriceAlreadyExists(supplier, fromLocation, toLocation)

    return Price(
      supplier = supplier,
      fromLocation = fromLocation,
      toLocation = toLocation,
      priceInPence = price,
      effectiveYear = effectiveYear
    )
  }

  private fun failIfPriceAlreadyExists(supplier: Supplier, fromLocation: Location, toLocation: Location) {
    pricesRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      supplier,
      fromLocation,
      toLocation,
      effectiveYear
    )?.let {
      throw RuntimeException("Duplicate price: '${fromLocation.siteName}' to '${toLocation.siteName}' for $supplier")
    }
  }

  fun addError(row: Row, error: Throwable) = errors.add(
    PricesSpreadsheetError(
      supplier,
      row.rowNum + ROW_OFFSET,
      error.cause?.cause
        ?: error
    )
  )

  /**
   * Strips all whitespace and converts to uppercase.  If blank then returns null.
   */
  private fun Row.getFormattedStringCell(cell: Int): String? {
    return getCell(cell).stringCellValue.uppercase().trim().takeIf { it.isNotBlank() }
  }

  override fun close() {
    spreadsheet.close()
  }
}
