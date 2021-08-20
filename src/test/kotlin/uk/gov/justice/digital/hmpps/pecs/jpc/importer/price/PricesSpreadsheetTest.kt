package uk.gov.justice.digital.hmpps.pecs.jpc.importer.price

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

internal class PricesSpreadsheetTest {

  private val workbook: Workbook = XSSFWorkbook()
  private val workbookSpy: Workbook = mock { spy(workbook) }
  private val sheet: Sheet = workbook.createSheet()
  private val priceRepository: PriceRepository = mock()

  @Nested
  inner class RecordingErrors {

    private val spreadsheet: PricesSpreadsheet = PricesSpreadsheet(workbookSpy, Supplier.GEOAMEY, emptyList(), priceRepository, 2020)

    private val row: Row = sheet.createRow(0)

    @Test
    internal fun `no errors by default`() {
      assertThat(spreadsheet.errors).isEmpty()
    }

    @Test
    internal fun `errors are recorded`() {
      val exception = RuntimeException("something went wrong")

      spreadsheet.addError(row, exception)

      assertThat(spreadsheet.errors).containsOnly(PricesSpreadsheetError(Supplier.GEOAMEY, row.rowNum + 1, exception))
    }
  }

  @Nested
  inner class MappingRowToPrice {

    private val price: Price = mock()
    private val fromLocation: Location = Location(LocationType.CC, "from agency id", "from site")
    private val toLocation: Location = Location(LocationType.CC, "to agency id", "to site")

    private val spreadsheet: PricesSpreadsheet =
      PricesSpreadsheet(workbookSpy, Supplier.GEOAMEY, listOf(fromLocation, toLocation), priceRepository, 2020)

    private val row: Row = sheet.createRow(1).apply {
      this.createCell(0).setCellValue(1.0)
      this.createCell(1).setCellValue("from site")
      this.createCell(2).setCellValue("to site")
      this.createCell(3).setCellValue(100.005)
    }

    @Test
    internal fun `can map row to price`() {
      val price = spreadsheet.mapToPrice(row)

      assertThat(price.fromLocation).isEqualTo(fromLocation)
      assertThat(price.toLocation).isEqualTo(toLocation)
      assertThat(price.priceInPence).isEqualTo(10001)
    }

    @Test
    internal fun `throws error if from location is blank`() {
      row.getCell(1).setCellValue("")

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("From location name cannot be blank")
    }

    @Test
    internal fun `throws error if price is zero`() {
      row.getCell(3).setCellValue(0.0)

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Price must be greater than zero")
    }

    @Test
    internal fun `cannot map row to price when from site not found`() {
      row.getCell(1).setCellValue("UNKNOWN from site")

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("From location 'UNKNOWN FROM SITE' for supplier 'GEOAMEY' not found")
    }

    @Test
    internal fun `throws error if to location is blank`() {
      row.getCell(2).setCellValue("")

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("To location name cannot be blank")
    }

    @Test
    internal fun `throws error if problem reading price`() {
      row.getCell(3).setCellValue("string instead of numeric")

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Error retrieving price for supplier 'GEOAMEY'")
    }

    @Test
    internal fun `cannot map row to price when to site not found`() {
      row.getCell(2).setCellValue("UNKNOWN to site")

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("To location 'UNKNOWN TO SITE' for supplier 'GEOAMEY' not found")
    }

    @Test
    internal fun `throws error if duplicate price`() {
      whenever(priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(Supplier.GEOAMEY, fromLocation, toLocation, 2020)).thenReturn(price)

      assertThatThrownBy { spreadsheet.mapToPrice(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Duplicate price: 'from site' to 'to site' for GEOAMEY")
    }
  }
}
