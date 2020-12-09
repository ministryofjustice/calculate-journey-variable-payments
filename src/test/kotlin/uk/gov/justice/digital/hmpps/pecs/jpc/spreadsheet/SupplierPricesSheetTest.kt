package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveDate
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.util.*

@SpringJUnitConfig(TestConfig::class)
internal class SupplierPricesSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    private val header: PriceSheet.Header =PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO)

    private val supplierPricesSheet: SupplierPricesSheet = SupplierPricesSheet(workbook, header)

    @Test
    internal fun `fails if output sheet is missing`() {
        assertThatThrownBy { SupplierPricesSheet(XSSFWorkbook(), header) }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    internal fun `prices are copied over and sorted to output spreadsheet`() {
        supplierPricesSheet.writePrices(listOf(
                Price(UUID.randomUUID(), Supplier.SERCO ,location("FROM SITE B"), location("TO SITE B"), 10024, effectiveYear = 2020),
                Price(UUID.randomUUID(), Supplier.SERCO , location("FROM SITE A"), location("TO SITE A"), 20059, effectiveYear = 2020)))

        assertOnPriceRow(supplierPricesSheet.sheet.getRow(10), PriceRow("FROM SITE A", "TO SITE A", 200.59))
        assertOnPriceRow(supplierPricesSheet.sheet.getRow(11), PriceRow("FROM SITE B", "TO SITE B", 100.24))
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
