package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.io.Closeable

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3
private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 2

/**
 * Simple wrapper class to encapsulate the logic around access to data in the locations spreadsheet. When finished with the spreadsheet should be closed.
 */
class LocationsSpreadsheet(private val spreadsheet: Workbook) : Closeable {

    enum class Tab(val label: String) {
        COURT("Courts"),
        HOSPITAL("Hospitals"),
        IMMIGRATION("Immigration"),
        OTHER("Other"),
        POLICE("Police"),
        PRISON("Prisons"),
        STCSCH("STC&SCH");
    }

    val errors: MutableList<LocationsSpreadsheetError> = mutableListOf()

    /**
     * Only rows containing locations are returned. The heading row is not included.
     */
    fun getRowsFrom(tab: Tab): List<Row> = spreadsheet.getSheet(tab.label).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

    fun mapToLocation(cells: List<Cell>): Location {
        return Location(
                locationType = LocationType.map(cells[TYPE].stringCellValue.toUpperCase().trim())
                        ?: throw IllegalArgumentException("Unsupported location type: " + cells[TYPE].stringCellValue),
                nomisAgencyId = cells[AGENCY].stringCellValue.trim(),
                siteName = cells[SITE].stringCellValue.toUpperCase().trim())
    }

    fun mapToLocation(row: Row): Location = mapToLocation(row.toList())

    fun addError(tab: Tab, row: Row, error: Throwable) = errors.add(LocationsSpreadsheetError(tab, row.rowNum + ROW_OFFSET, error.cause?.cause ?: error))

    override fun close() {
        spreadsheet.close()
    }
}
