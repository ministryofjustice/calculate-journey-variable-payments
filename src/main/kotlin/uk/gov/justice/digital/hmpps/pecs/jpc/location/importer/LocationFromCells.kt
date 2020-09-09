package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

private const val TYPE = 1
private const val SITE = 2
private const val AGENCY = 3

open class LocationFromCells(val index: Int, val name: String) {
    fun location(cells: List<Cell>) : Location =
            Location(
                    locationType = cells[TYPE].stringCellValue.toUpperCase().trim(),
                    nomisAgencyId = cells[AGENCY].stringCellValue.trim(),
                    siteName = cells[SITE].stringCellValue.toUpperCase().trim())


    fun sheet(workbook : XSSFWorkbook) : Sheet = workbook.getSheetAt(index);
}