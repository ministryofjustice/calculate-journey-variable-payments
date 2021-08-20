package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.util.UUID
import java.util.stream.Stream

internal class SupplierPricesSheetTest {

  private val header: PriceSheet.Header = PriceSheet.Header(
    defaultMoveDate10Sep2020,
    ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
    Supplier.SERCO
  )

  private val supplierPricesSheet: SupplierPricesSheet = SupplierPricesSheet(SXSSFWorkbook(), header)

  @Test
  internal fun `prices are copied over output spreadsheet`() {
    supplierPricesSheet.write(
      Stream.of(
        Price(
          UUID.randomUUID(),
          Supplier.SERCO,
          location("FROM SITE A"),
          location("TO SITE A"),
          20059,
          effectiveYear = 2020
        ),
        Price(
          UUID.randomUUID(),
          Supplier.SERCO,
          location("FROM SITE B"),
          location("TO SITE B"),
          10024,
          effectiveYear = 2020
        )
      )
    )

    assertOnSheetName(supplierPricesSheet, "JPC Price book")
    assertOnColumnDataHeadings(supplierPricesSheet, "Pick up", "Drop off", "Unit price")
    assertOnPriceRow(supplierPricesSheet.sheet.getRow(9), PriceRow("FROM SITE A", "TO SITE A", 200.59))
    assertOnPriceRow(supplierPricesSheet.sheet.getRow(10), PriceRow("FROM SITE B", "TO SITE B", 100.24))
  }

  private fun assertOnPriceRow(row: Row, priceRow: PriceRow) {
    assertThat(row.getCell(0).stringCellValue).isEqualTo(priceRow.fromSite)
    assertThat(row.getCell(1).stringCellValue).isEqualTo(priceRow.toSite)
    assertThat(row.getCell(2).numericCellValue).isEqualTo(priceRow.price)
    assertThat(row.getCell(2).cellStyle.dataFormatString).isEqualTo("\"£\"#,##0.00_);[Red](\"£\"#,##0.00)")
  }

  fun location(siteName: String) = Location(LocationType.CC, "x", siteName)

  internal data class PriceRow(val fromSite: String, val toSite: String, val price: Double)
}
