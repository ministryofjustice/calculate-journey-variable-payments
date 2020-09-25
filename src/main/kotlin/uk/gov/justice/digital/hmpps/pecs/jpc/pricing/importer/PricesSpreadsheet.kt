package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.io.Closeable

private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 2

/**
 * Simple wrapper class to encapsulate the logic around access to data in the supplier prices spreadsheet. When finished with the spreadsheet should be closed.
 */
class PricesSpreadsheet(private val spreadsheet: Workbook, val supplier: Supplier, private val locationRepo: LocationRepository) : Closeable {

    val errors: MutableList<PricesSpreadsheetError> = mutableListOf()

    /**
     * Only rows containing prices are returned. The heading row is not included.
     */
    fun getRows(): List<Row> = spreadsheet.getSheetAt(0).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

    fun mapToPrice(row: Row) = getPriceResult(supplier, row.toList())

    private fun getPriceResult(supplier: Supplier, cells: List<Cell>): Price {
        val journeyId = cells[0].numericCellValue
        val fromLocationName = cells[1].stringCellValue.toUpperCase().trim()
        val toLocationName = cells[2].stringCellValue.toUpperCase().trim()
        val price = cells[3].numericCellValue

        val fromLocation = locationRepo.findBySiteName(fromLocationName)
                ?: throw RuntimeException("Missing from location $fromLocationName for supplier $supplier")

        val toLocation = locationRepo.findBySiteName(toLocationName)
                ?: throw RuntimeException("Missing to location $toLocationName for supplier $supplier")

        return Price(
                supplier = supplier,
                fromLocationName = fromLocationName,
                fromLocationId = fromLocation.id,
                toLocationName = toLocationName,
                toLocationId = toLocation.id,
                journeyId = journeyId.toInt(),
                priceInPence = (price * 100).toInt())
    }

    fun addError(row: Row, error: Throwable) = errors.add(PricesSpreadsheetError(supplier, row.rowNum + ROW_OFFSET, error.cause?.cause ?: error))

    override fun close() {
        spreadsheet.close()
    }
}
