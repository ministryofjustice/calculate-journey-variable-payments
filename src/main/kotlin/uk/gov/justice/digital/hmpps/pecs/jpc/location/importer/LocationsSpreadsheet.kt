package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.io.Closeable

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3
private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 1

/**
 * Simple wrapper class to encapsulate the logic around access to data in the locations spreadsheet. When finished with the spreadsheet should be closed.
 */
class LocationsSpreadsheet(private val spreadsheet: Workbook, private val locationRepository: LocationRepository) : Closeable {

    init {
        Tab.values().toList().filter { spreadsheet.getSheet(it.label) == null }.joinToString { it.label }
                .takeIf { it.isNotBlank() }?.let { throw NullPointerException("The following tabs are missing from the locations spreadsheet: $it") }
    }

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
        val locationType = LocationType.map(cells[TYPE].stringCellValue.toUpperCase().trim())
                ?: throw IllegalArgumentException("Unsupported location type: " + cells[TYPE].stringCellValue)

        val agency = cells[AGENCY].stringCellValue.toUpperCase().trim().takeUnless { it.isBlank() }
                ?: throw NullPointerException("Agency id cannot be blank")

        val site = cells[SITE].stringCellValue.toUpperCase().trim().takeUnless { it.isBlank() }
                ?: throw NullPointerException("Site name cannot be blank")

        locationRepository.findByNomisAgencyId(agency).let { if (it != null) throw IllegalArgumentException("Agency id '$agency' already exists") }

        return Location(
                locationType = locationType,
                nomisAgencyId = agency,
                siteName = site)
    }

    fun mapToLocation(row: Row): Location = mapToLocation(row.toList())

    fun addError(tab: Tab, row: Row, error: Throwable) = errors.add(LocationsSpreadsheetError(tab, row.rowNum + ROW_OFFSET, error.cause?.cause
            ?: error))

    override fun close() {
        spreadsheet.close()
    }
}
