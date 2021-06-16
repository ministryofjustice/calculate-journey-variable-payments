package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import java.util.stream.Stream

class LocationsSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.createSheet("Locations"), header) {
  override fun writeMove(move: Move) {}

  @Deprecated("Use write locations as stream instead", ReplaceWith("write(locations)"))
  fun writeLocations(locations: List<Location>) = write(locations.stream())

  fun write(locations: Stream<Location>) {
    locations.use {
      it.forEach { location ->
        val row = createRow()
        row.addCell(0, location.nomisAgencyId)
        row.addCell(1, location.siteName)
        row.addCell(2, location.locationType.name)
      }
    }
  }
}
