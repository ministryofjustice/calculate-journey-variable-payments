package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.LOCATION_TYPE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.NAME
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.NOMIS_AGENCY_ID
import java.util.stream.Stream

class LocationsSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Locations"),
  header = header,
  dataColumns = listOf(NOMIS_AGENCY_ID, NAME, LOCATION_TYPE)
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
