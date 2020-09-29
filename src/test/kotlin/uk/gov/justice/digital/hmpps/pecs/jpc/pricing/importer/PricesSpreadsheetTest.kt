package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import com.nhaarman.mockitokotlin2.*
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

internal class PricesSpreadsheetTest {

    private val workbook: Workbook = XSSFWorkbook()
    private val workbookSpy: Workbook = mock { spy(workbook) }
    private val sheet: Sheet = workbook.createSheet()
    private val locationRepository: LocationRepository = mock()
    private val priceRepository: PriceRepository = mock()
    private val spreadsheet: PricesSpreadsheet = PricesSpreadsheet(workbookSpy, Supplier.GEOAMEY, locationRepository, priceRepository)

    @Nested
    inner class RecordingErrors {

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
        private val location: Location = Location(LocationType.CC, "agency id", "site name")

        private val row: Row = sheet.createRow(1).apply {
            this.createCell(0).setCellValue(1.0)
            this.createCell(1).setCellValue("from site")
            this.createCell(2).setCellValue("to site")
            this.createCell(3).setCellValue(100.00)
        }

        @Test
        internal fun `can map row to price`() {
            whenever(locationRepository.findBySiteName(any())).thenReturn(location)
            whenever(priceRepository.findByFromLocationNameAndToLocationName(any(), any())).thenReturn(null)

            assertThatCode { spreadsheet.mapToPrice(row) }.doesNotThrowAnyException()
        }

        @Test
        internal fun `throws error if from location is blank`() {
            row.getCell(1).setCellValue("")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("From location name cannot be blank")
        }

        @Test
        internal fun `cannot map row to price when from site not found`() {
            whenever(locationRepository.findBySiteName("FROM SITE")).thenReturn(location)
            row.getCell(1).setCellValue("unknown from site")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("From location 'UNKNOWN FROM SITE' for supplier 'GEOAMEY' not found")
        }

        @Test
        internal fun `throws error if to location is blank`() {
            row.getCell(2).setCellValue("")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("To location name cannot be blank")
        }

        @Test
        internal fun `throws error if problem reading price`() {
            row.getCell(3).setCellValue("string instead of numeric")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Error retrieving price for supplier 'GEOAMEY'")
        }

        @Test
        internal fun `throws error if problem reading journey id`() {
            row.getCell(0).setCellValue("string instead of numeric")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Error retrieving journey id for supplier 'GEOAMEY'")
        }

        @Test
        internal fun `cannot map row to price when to site not found`() {
            whenever(locationRepository.findBySiteName(any())).thenReturn(location)
            whenever(locationRepository.findBySiteName("UNKNOWN TO SITE")).thenReturn(null)
            row.getCell(2).setCellValue("unknown to site")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("To location 'UNKNOWN TO SITE' for supplier 'GEOAMEY' not found")
        }

        @Test
        internal fun `cannot map row to price when duplicate price entry`() {
            whenever(locationRepository.findBySiteName(any())).thenReturn(location)
            whenever(priceRepository.findByFromLocationNameAndToLocationName(any(), any())).thenReturn(price)

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Duplicate price with from location 'FROM SITE' and to location 'TO SITE' for supplier 'GEOAMEY'")
        }
    }

    @Test
    internal fun `prices spreadsheet is closed cleanly`() {
        spreadsheet.close()

        verify(workbookSpy).close()
    }
}
