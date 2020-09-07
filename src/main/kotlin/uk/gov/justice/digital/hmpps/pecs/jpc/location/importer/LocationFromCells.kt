package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.ss.usermodel.Cell
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import com.github.kittinunf.result.Result

interface LocationFromCells {

    val sheetIndex: Int
    val sheetName: String

    fun getLocationResult(cells: List<Cell>) : Result<Location, Exception> {

        return try {
            Result.success(Location(
                    locationType = cells[1].stringCellValue.toUpperCase().trim(),
                    nomisAgencyId = cells[3].stringCellValue.trim(),
                    siteName = cells[2].stringCellValue.toUpperCase().trim()
            ))
        } catch (e: Exception) {
            return Result.error(e)
        }
    }
}