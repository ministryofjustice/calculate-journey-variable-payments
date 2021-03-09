package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move

class LocationsSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Locations")!!, header) {
  override fun writeMove(move: Move) {}

  fun writeLocations(locations: List<Location>) {
    locations.forEach {
      val row = createRow()
      row.addCell(0, it.nomisAgencyId)
      row.addCell(1, it.siteName)
      row.addCell(2, it.locationType.name)
    }
  }
}
