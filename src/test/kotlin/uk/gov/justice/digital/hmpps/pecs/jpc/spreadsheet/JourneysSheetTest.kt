package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@SpringJUnitConfig(TestConfig::class)
internal class JourneysSheetTest(@Autowired private val template: JPCTemplateProvider) {

  private val workbook: Workbook = XSSFWorkbook(template.get())

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

    val sheet = JourneysSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    sheet.writeJourneys(listOf(journey))

    assertCellEquals(sheet, 9, 0, "from") // from site name
    assertCellEquals(sheet, 9, 1, "TO") // TO - NOMIS Agency ID because there is no site name
    assertCellEquals(sheet, 9, 2, 22.0) // volume
    assertCellEquals(sheet, 9, 3, 22.0) // billable journey count
    assertCellEquals(sheet, 9, 4, 1.0) // unit price in pounds
    assertCellEquals(sheet, 9, 5, 22.0) // total price in pounds
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
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    sheet.writeJourneys(listOf(journey))

    assertCellEquals(sheet, 9, 0, "from") // from site name
    assertCellEquals(sheet, 9, 1, "TO") // TO - NOMIS Agency ID because there is no site name
    assertCellEquals(sheet, 9, 2, 22.0) // volume
    assertCellEquals(sheet, 9, 3, 0.0) // billable journey count
    assertCellEquals(sheet, 9, 4, "NOT PRESENT") // no unit price in pounds
    assertCellEquals(sheet, 9, 5, 0.00) // total price in pounds
  }
}
