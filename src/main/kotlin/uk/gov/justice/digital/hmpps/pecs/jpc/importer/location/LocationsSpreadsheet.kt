package uk.gov.justice.digital.hmpps.pecs.jpc.importer.location

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.InboundSpreadsheet
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3
private const val COLUMN_HEADINGS = 1
private const val ROW_OFFSET = 1

/**
 * Simple wrapper class to encapsulate the logic around access to data in the locations spreadsheet. When finished with the spreadsheet should be closed.
 */
class LocationsSpreadsheet(private val spreadsheet: Workbook, private val locationRepository: LocationRepository) :
  InboundSpreadsheet(spreadsheet) {

  init {
    val missingTabs = Tab.values().filter { spreadsheet.getSheet(it.label) == null }.toList()

    if (missingTabs.isNotEmpty()) throw RuntimeException("The following tabs are missing from the locations spreadsheet: ${missingTabs.joinToString { it.label }}")
  }

  enum class Tab(val label: String) {
    COURT("Courts"),
    HOSPITAL("Hospitals"),
    IMMIGRATION("Immigration"),
    OTHER("Other"),
    POLICE("Police"),
    PRISON("Prisons"),
    PROBATION("Probation"),
    STCSCH("STC&SCH");
  }

  val errors: MutableList<LocationsSpreadsheetError> = mutableListOf()

  /**
   * Iterates through all data rows excluding the heading row.  Any errors will be caught and recorded against the spreadsheet.
   */
  fun forEachRowOn(tab: Tab, f: (location: Location) -> Unit) {
    getRowsFrom(tab).forEach { row ->
      Result.runCatching { f(toLocation(row)) }.onFailure { this.addError(tab, row, it) }
    }
  }

  /**
   * Only rows containing locations are returned. The heading row is not included.
   */
  private fun getRowsFrom(tab: Tab): List<Row> =
    spreadsheet.getSheet(tab.label).drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

  fun toLocation(row: Row): Location {
    val locationType = LocationType.map(row.getFormattedStringCell(TYPE)!!)
      ?: throw RuntimeException("Unsupported location type: " + row.getFormattedStringCell(TYPE))
    val agency = row.getFormattedStringCell(AGENCY) ?: throw RuntimeException("Agency id cannot be blank")
    val site = row.getFormattedStringCell(SITE) ?: throw RuntimeException("Site name cannot be blank")
    if (site.length > 255) throw RuntimeException("Site name cannot be more than 255 characters long.")

    locationRepository.findByNomisAgencyId(agency)
      .let { if (it != null) throw RuntimeException("Agency id '$agency' already exists") }

    locationRepository.findBySiteName(site)
      .let { if (it != null) throw RuntimeException("Site name '$site' already exists") }

    return Location(
      locationType = locationType,
      nomisAgencyId = agency,
      siteName = site
    )
  }

  fun addError(tab: Tab, row: Row, error: Throwable) =
    errors.add(LocationsSpreadsheetError(tab, row.rowNum + ROW_OFFSET, error.cause?.cause ?: error))
}
