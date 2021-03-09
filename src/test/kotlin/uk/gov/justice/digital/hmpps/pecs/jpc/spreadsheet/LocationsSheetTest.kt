package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@SpringJUnitConfig(TestConfig::class)
internal class LocationsSheetTest(@Autowired private val template: JPCTemplateProvider) {

  private val workbook: Workbook = XSSFWorkbook(template.get())

  @Test
  internal fun `summary prices`() {

    val location1 = Location(LocationType.PR, "LOCATION1", "Location 1")
    val location2 = Location(LocationType.AP, "LOCATION2", "Location 2")
    val locations = listOf(location1, location2)

    val sheet = LocationsSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )
    sheet.writeLocations(locations)

    assertCellEquals(sheet, 9, 0, "LOCATION1")
    assertCellEquals(sheet, 9, 1, "Location 1")
    assertCellEquals(sheet, 9, 2, "PR")

    assertCellEquals(sheet, 10, 0, "LOCATION2")
    assertCellEquals(sheet, 10, 1, "Location 2")
    assertCellEquals(sheet, 10, 2, "AP")
  }
}
