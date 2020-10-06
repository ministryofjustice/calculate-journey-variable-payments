package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.InboundSpreadsheet
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

private const val JOURNEY_ID = 0
private const val FROM_LOCATION = 1
private const val TO_LOCATION = 2
private const val PRICE = 3
private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 1

/**
 * Simple wrapper class to encapsulate the logic around access to data in the supplier prices spreadsheet. When finished with the spreadsheet should be closed.
 */
class PricesSpreadsheet(private val spreadsheet: Workbook,
                        val supplier: Supplier,
                        private val locationRepo: LocationRepository,
                        private val priceRepository: PriceRepository) : InboundSpreadsheet(spreadsheet) {

    val errors: MutableList<PricesSpreadsheetError> = mutableListOf()

    fun forEachRow(f: (price: Price) -> Unit) {
        getRows().forEach { row -> Result.runCatching { f(mapToPrice(row)) }.onFailure { this.addError(row, it) } }
    }

    /**
     * Only rows containing prices are returned. The heading row is not included.
     */
    private fun getRows(): List<Row> = spreadsheet.getSheetAt(0).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

    fun mapToPrice(row: Row) = getPrice(supplier, row)

    private fun getPrice(supplier: Supplier, row: Row): Price {
        val journeyId = Result.runCatching { row.getCell(JOURNEY_ID).numericCellValue }.getOrElse { throw RuntimeException("Error retrieving journey id for supplier '$supplier'", it) }
        val fromLocationName = row.getFormattedStringCell(FROM_LOCATION)
                ?: throw RuntimeException("From location name cannot be blank")
        val toLocationName = row.getFormattedStringCell(TO_LOCATION)
                ?: throw RuntimeException("To location name cannot be blank")
        val price = Result.runCatching { (row.getCell(PRICE).numericCellValue * 100).toInt() }
                .onSuccess { if (it == 0) throw RuntimeException("Price must be greater than zero") }
                .getOrElse { throw RuntimeException("Error retrieving price for supplier '$supplier'", it) }

        val fromLocation = locationRepo.findBySiteName(fromLocationName)
                ?: throw RuntimeException("From location '$fromLocationName' for supplier '$supplier' not found")
        val toLocation = locationRepo.findBySiteName(toLocationName)
                ?: throw RuntimeException("To location '$toLocationName' for supplier '$supplier' not found")

        priceRepository.findByFromLocationNameAndToLocationName(fromLocation.siteName, toLocation.siteName)?.let {
            throw RuntimeException("Duplicate price with from location '$fromLocationName' and to location '$toLocationName' for supplier '$supplier'")
        }

        return Price(
                supplier = supplier,
                fromLocationName = fromLocationName,
                fromLocationId = fromLocation.id,
                toLocationName = toLocationName,
                toLocationId = toLocation.id,
                journeyId = journeyId.toInt(),
                priceInPence = price)
    }

    fun addError(row: Row, error: Throwable) = errors.add(PricesSpreadsheetError(supplier, row.rowNum + ROW_OFFSET, error.cause?.cause
            ?: error))
}
