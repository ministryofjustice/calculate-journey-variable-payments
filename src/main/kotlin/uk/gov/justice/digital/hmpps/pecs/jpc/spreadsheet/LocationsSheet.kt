package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import java.util.stream.Stream

class LocationsSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Locations"),
  header = header,
  dataColumnHeadings = listOf("NOMIS Agency ID", "Name", "Type")
) {
  override fun writeMove(move: Move) {}

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
