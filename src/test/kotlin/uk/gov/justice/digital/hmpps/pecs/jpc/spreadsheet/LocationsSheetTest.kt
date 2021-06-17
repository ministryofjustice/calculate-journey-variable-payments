package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.util.stream.Stream

internal class LocationsSheetTest {

  private val workbook: Workbook = SXSSFWorkbook()

  @Test
  internal fun `locations details`() {
    val location1 = Location(LocationType.PR, "LOCATION1", "B Location 1")
    val location2 = Location(LocationType.AP, "LOCATION2", "A Location 2")

    val sheet = LocationsSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )

    sheet.write(Stream.of(location1, location2))

    sheet.sheet.getRow(8).apply {
      assertThat(getCell(0).stringCellValue).isEqualTo("NOMIS Agency ID")
      assertThat(getCell(1).stringCellValue).isEqualTo("Name")
      assertThat(getCell(2).stringCellValue).isEqualTo("Type")
    }

    assertCellEquals(sheet, 9, 0, "LOCATION1")
    assertCellEquals(sheet, 9, 1, "B Location 1")
    assertCellEquals(sheet, 9, 2, "PR")

    assertCellEquals(sheet, 10, 0, "LOCATION2")
    assertCellEquals(sheet, 10, 1, "A Location 2")
    assertCellEquals(sheet, 10, 2, "AP")
  }
}
