package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3

/**
 * Represents the supported location tabs in the locations spreadsheet,
 */
enum class LocationTab(val tabName: String) {

    COURT("Courts"),
    HOSPITAL("Hospitals"),
    IMMIGRATION("Immigration"),
    OTHER("Other"),
    POLICE("Police"),
    PRISON("Prisons"),
    STCSCH("STC&SCH");

    fun map(cells: List<Cell>): Location {
        return Location(
                locationType = cells[TYPE].stringCellValue.toUpperCase().trim(),
                nomisAgencyId = cells[AGENCY].stringCellValue.trim(),
                siteName = cells[SITE].stringCellValue.toUpperCase().trim())
    }

    fun map(cells: Row): Location = map(cells.toList())

    fun sheet(locationsWorkbook: XSSFWorkbook): Sheet = locationsWorkbook.getSheet(tabName)
}
