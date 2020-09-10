package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3

enum class LocationType(val index: Int, val label: String) {

    COURT(5, "COURT"),
    HOSPITAL(9, "HOSPITALS"),
    IMMIGRATION(10, "IMMIGRATION"),
    OTHER(12, "OTHER"),
    POLICE(6, "POLICE"),
    PRISON(8, "PRISON"),
    STCSCH(11, "STC&SCH");

    fun toLocation(cells: List<Cell>) : Location =
            Location(
                    locationType = cells[TYPE].stringCellValue.toUpperCase().trim(),
                    nomisAgencyId = cells[AGENCY].stringCellValue.trim(),
                    siteName = cells[SITE].stringCellValue.toUpperCase().trim())

    fun toLocation(cells: Row) : Location = toLocation(cells.toList())

    fun sheet(workbook : XSSFWorkbook) : Sheet = workbook.getSheetAt(index)
}
