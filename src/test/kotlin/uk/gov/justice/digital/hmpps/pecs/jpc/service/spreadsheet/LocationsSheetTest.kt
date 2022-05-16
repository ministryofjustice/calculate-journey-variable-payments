package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.util.stream.Stream

internal class LocationsSheetTest {

  @Test
  internal fun `locations details`() {
    val location1 = Location(LocationType.PR, "LOCATION1", "B Location 1")
    val location2 = Location(LocationType.AP, "LOCATION2", "A Location 2")

    val locationsSheet = LocationsSheet(
      SXSSFWorkbook(),
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        DateRange(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )

    locationsSheet.write(Stream.of(location1, location2))

    assertOnSheetName(locationsSheet, "Locations")
    assertOnColumnDataHeadings(locationsSheet, "NOMIS Agency ID", "Name", "Location type")

    assertCellEquals(locationsSheet, 9, 0, "LOCATION1")
    assertCellEquals(locationsSheet, 9, 1, "B Location 1")
    assertCellEquals(locationsSheet, 9, 2, "PR")

    assertCellEquals(locationsSheet, 10, 0, "LOCATION2")
    assertCellEquals(locationsSheet, 10, 1, "A Location 2")
    assertCellEquals(locationsSheet, 10, 2, "AP")
  }
}
