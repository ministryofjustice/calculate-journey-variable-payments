package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

internal class PricesSpreadsheetTest {

    private val workbook: Workbook = XSSFWorkbook()
    private val sheet: Sheet = workbook.createSheet()
    private val location: Location = Location(LocationType.CC, "agency id", "site name")
    private val locationRepository: LocationRepository = mock()
    private val spreadsheet: PricesSpreadsheet = PricesSpreadsheet(workbook, Supplier.GEOAMEY, locationRepository)

    @Nested
    inner class RecordingErrors {

        private val row: Row = sheet.createRow(0)

        @Test
        fun `no errors by default`() {
            assertThat(spreadsheet.errors).isEmpty()
        }

        @Test
        fun `errors are recorded`() {
            val exception = RuntimeException("something went wrong")

            spreadsheet.addError(row, exception)

            assertThat(spreadsheet.errors).containsOnly(PricesSpreadsheetError(Supplier.GEOAMEY, row.rowNum + 1, exception))
        }
    }

    @Nested
    inner class MappingRowsPrice {

        private val row: Row = sheet.createRow(1).apply {
            this.createCell(0).setCellValue(1.0)
            this.createCell(1).setCellValue("from site")
            this.createCell(2).setCellValue("to site")
            this.createCell(3).setCellValue(100.00)
        }

        @Test
        fun `can map row to price`() {
            whenever(locationRepository.findBySiteName(any())).thenReturn(location)

            assertThatCode { spreadsheet.mapToPrice(row) }.doesNotThrowAnyException()
        }

        @Test
        fun `cannot map row to price when from site not found`() {
            whenever(locationRepository.findBySiteName("unknown from site")).thenReturn(null)
            row.getCell(1).setCellValue("UNKNOWN FROM SITE")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(RuntimeException::class.java)
                    .withFailMessage("Missing from location UNKNOWN FROM SITE for supplier GEOAMEY")
        }

        @Test
        fun `cannot map row to price when to site not found`() {
            whenever(locationRepository.findBySiteName("unknown to site")).thenReturn(null)
            row.getCell(2).setCellValue("UNKNOWN TO SITE")

            assertThatThrownBy { spreadsheet.mapToPrice(row) }
                    .isInstanceOf(RuntimeException::class.java)
                    .withFailMessage("Missing from location UNKNOWN TO SITE for supplier GEOAMEY")
        }
    }

    @AfterEach
    fun after() {
        spreadsheet.close()
    }
}
