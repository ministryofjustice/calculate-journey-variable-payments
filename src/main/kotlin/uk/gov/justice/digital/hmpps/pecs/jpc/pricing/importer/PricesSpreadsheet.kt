package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.io.Closeable

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
                        private val priceRepository: PriceRepository) : Closeable {

    val errors: MutableList<PricesSpreadsheetError> = mutableListOf()

    /**
     * Only rows containing prices are returned. The heading row is not included.
     */
    fun getRows(): List<Row> = spreadsheet.getSheetAt(0).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

    fun mapToPrice(row: Row) = getPriceResult(supplier, row.toList())

    private fun getPriceResult(supplier: Supplier, cells: List<Cell>): Price {
        val journeyId = Result.runCatching { cells[JOURNEY_ID].numericCellValue }.getOrElse { throw IllegalArgumentException("Error retrieving journey id for supplier '$supplier'", it) }

        val fromLocationName = cells[FROM_LOCATION].stringCellValue.toUpperCase().trim().takeUnless { it.isBlank() }
                ?: throw NullPointerException("From location name cannot be blank")
        val toLocationName = cells[TO_LOCATION].stringCellValue.toUpperCase().trim().takeUnless { it.isBlank() }
                ?: throw NullPointerException("To location name cannot be blank")

        val price = Result.runCatching { cells[PRICE].numericCellValue }.getOrElse { throw IllegalArgumentException("Error retrieving price for supplier '$supplier'", it) }

        val fromLocation = locationRepo.findBySiteName(fromLocationName)
                ?: throw NullPointerException("From location '$fromLocationName' for supplier '$supplier' not found")

        val toLocation = locationRepo.findBySiteName(toLocationName)
                ?: throw NullPointerException("To location '$toLocationName' for supplier '$supplier' not found")

        priceRepository.findByFromLocationNameAndToLocationName(fromLocation.siteName, toLocation.siteName)?.let {
            throw IllegalArgumentException("Duplicate price with from location '$fromLocationName' and to location '$toLocationName' for supplier '$supplier'")
        }

        return Price(
                supplier = supplier,
                fromLocationName = fromLocationName,
                fromLocationId = fromLocation.id,
                toLocationName = toLocationName,
                toLocationId = toLocation.id,
                journeyId = journeyId.toInt(),
                priceInPence = (price * 100).toInt())
    }

    fun addError(row: Row, error: Throwable) = errors.add(PricesSpreadsheetError(supplier, row.rowNum + ROW_OFFSET, error.cause?.cause
            ?: error))

    override fun close() {
        spreadsheet.close()
    }
}
