package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020

internal class JourneysSheetTest {

  @Test
  internal fun `test unique journeys`() {

    val journey = JourneyWithPrice(
      fromNomisAgencyId = "FRO",
      fromLocationType = LocationType.PR,
      fromSiteName = "from",
      toNomisAgencyId = "TO",
      toLocationType = null,
      toSiteName = null,
      unitPriceInPence = 100,
      volume = 22,
      totalPriceInPence = 2200
    )

    val journeysSheet = JourneysSheet(
      SXSSFWorkbook(),
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    journeysSheet.writeJourneys(listOf(journey))

    assertOnSheetName(journeysSheet, "Journeys")
    assertOnSubheading(journeysSheet, "TOTAL VOLUME BY JOURNEY")
    assertOnColumnDataHeadings(journeysSheet, "Pick up", "Drop off", "Total journey count", "Billable journey count", "Unit price", "Total price")
    assertCellEquals(journeysSheet, 9, 0, "from") // from site name
    assertCellEquals(journeysSheet, 9, 1, "TO") // TO - NOMIS Agency ID because there is no site name
    assertCellEquals(journeysSheet, 9, 2, 22) // volume
    assertCellEquals(journeysSheet, 9, 3, 22) // billable journey count
    assertCellEquals(journeysSheet, 9, 4, 1.0) // unit price in pounds
    assertCellEquals(journeysSheet, 9, 5, 22.0) // total price in pounds
  }

  @Test
  internal fun `test unique journeys price not present`() {

    val journey = JourneyWithPrice(
      fromNomisAgencyId = "FRO",
      fromLocationType = LocationType.PR,
      fromSiteName = "from",
      toNomisAgencyId = "TO",
      toLocationType = null,
      toSiteName = null,
      unitPriceInPence = null,
      volume = 22,
      totalPriceInPence = 0
    )

    val sheet = JourneysSheet(
      SXSSFWorkbook(),
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    sheet.writeJourneys(listOf(journey))

    assertCellEquals(sheet, 9, 0, "from") // from site name
    assertCellEquals(sheet, 9, 1, "TO") // TO - NOMIS Agency ID because there is no site name
    assertCellEquals(sheet, 9, 2, 22) // volume
    assertCellEquals(sheet, 9, 3, 0) // billable journey count
    assertCellEquals(sheet, 9, 4, "NOT PRESENT") // no unit price in pounds
    assertCellEquals(sheet, 9, 5, 0.0) // total price in pounds
  }
}
