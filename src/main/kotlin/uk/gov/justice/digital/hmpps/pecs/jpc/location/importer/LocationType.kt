package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3
private const val ACTIVE_INACTIVE = 6

enum class LocationType(val label: String, private val activeFieldPosition: Int = ACTIVE_INACTIVE) {

    COURT("Courts", 8), // The position of this cell in the court tab column differs to the majority by two.
    HOSPITAL("Hospitals"),
    IMMIGRATION("Immigration"),
    OTHER("Other"),
    POLICE("Police", 7), // The position of this cell in the police tab column differs to the majority by one.
    PRISON("Prisons"),
    STCSCH("STC&SCH");

    /**
     * Will return null if location is deemed to be not active.
     */
    fun active(cells: List<Cell>): Location? {
        val active = "ACTIVE" == cells[this.activeFieldPosition].stringCellValue.toUpperCase().trim()

        return active.then(Location(
                locationType = cells[TYPE].stringCellValue.toUpperCase().trim(),
                nomisAgencyId = cells[AGENCY].stringCellValue.trim(),
                siteName = cells[SITE].stringCellValue.toUpperCase().trim()))
    }

    /**
     * Will return null if location is deemed to be not active.
     */
    fun active(cells: Row): Location? = active(cells.toList())

    fun sheet(workbook: XSSFWorkbook): Sheet = workbook.getSheet(label)

    private infix fun <T : Any> Boolean.then(param: T): T? = if (this) param else null
}
