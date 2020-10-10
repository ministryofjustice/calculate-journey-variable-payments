package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

internal class PricesSpreadsheetTest {

    private val workbook: Workbook = XSSFWorkbook()
    private val workbookSpy: Workbook = mock { spy(workbook) }
    private val sheet: Sheet = workbook.createSheet()

    @Nested
    inner class RecordingErrors {

        private val spreadsheet: PricesSpreadsheet = PricesSpreadsheet(workbookSpy, Supplier.GEOAMEY, emptyList())

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

        private val fromLocation: Location = Location(LocationType.CC, "from agency id", "from site")
        private val toLocation: Location = Location(LocationType.CC, "to agency id", "to site")

        private val spreadsheet: PricesSpreadsheet = PricesSpreadsheet(workbookSpy, Supplier.GEOAMEY, listOf(fromLocation, toLocation))

        private val row: Row = sheet.createRow(1).apply {
            this.createCell(0).setCellValue(1.0)
            this.createCell(1).setCellValue("from site")
            this.createCell(2).setCellValue("to site")
            this.createCell(3).setCellValue(100.00)
        }

        @Test
        internal fun `can map row to price`() {
            val price = spreadsheet.mapToPrice(row)

            assertThat(price.fromLocationId).isEqualTo(fromLocation.id)
            assertThat(price.fromLocationName).isEqualTo("FROM SITE")
            assertThat(price.toLocationId).isEqualTo(toLocation.id)
            assertThat(price.toLocationName).isEqualTo("TO SITE")
            assertThat(price.priceInPence).isEqualTo(10000)
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
            row.getCell(1).setCellValue("unknown from site")

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
        internal fun `throws error if problem reading journey id`() {
            row.getCell(0).setCellValue("string instead of numeric")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(RuntimeException::class.java)
                    .hasMessage("Error retrieving journey id for supplier 'GEOAMEY'")
        }

        @Test
        internal fun `cannot map row to price when to site not found`() {
            row.getCell(2).setCellValue("unknown to site")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(RuntimeException::class.java)
                    .hasMessage("To location 'UNKNOWN TO SITE' for supplier 'GEOAMEY' not found")
        }
    }
}
