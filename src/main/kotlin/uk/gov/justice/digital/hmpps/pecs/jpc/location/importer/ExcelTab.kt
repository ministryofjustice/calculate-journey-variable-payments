package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location

interface ExcelTab {

    val sheetIndex: Int
    val sheetName: String

    fun locationFromRowCells(cells: List<Cell>) : Location {
        return Location(
                locationType = cells[1].stringCellValue.toUpperCase().trim(),
                nomisAgencyId = cells[3].stringCellValue,
                siteName = cells[2].stringCellValue.toUpperCase().trim()
        )
    }
}