package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import com.nhaarman.mockitokotlin2.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

internal class LocationsSpreadsheetTest {

    private val sheet: Sheet = mock()
    private val workbook: Workbook = mock { on { it.getSheet( com.nhaarman.mockitokotlin2.any()) } doReturn sheet }
    private val location: Location = mock()
    private val locationRepository: LocationRepository = mock()
    private val spreadsheet: LocationsSpreadsheet = LocationsSpreadsheet(workbook, locationRepository)

    @Nested
    inner class Instantiation {
        @Test
        internal fun `fails instantiation if court and immigration tabs are missing the locations spreadsheet`() {
            LocationsSpreadsheet.Tab.values().forEach {
                if (it == LocationsSpreadsheet.Tab.COURT || it == LocationsSpreadsheet.Tab.IMMIGRATION)
                    whenever(workbook.getSheet(it.label)).thenReturn(null)
                else
                    whenever(workbook.getSheet(it.label)).thenReturn(sheet)
            }

            assertThatThrownBy { LocationsSpreadsheet(workbook, locationRepository) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("The following tabs are missing from the locations spreadsheet: Courts, Immigration")
        }

        @Test
        internal fun `succeeds instantiation when all tabs are present in the locations spreadsheet`() {
            LocationsSpreadsheet.Tab.values().forEach { whenever(workbook.getSheet(it.label)).thenReturn(sheet) }

            assertThatCode { LocationsSpreadsheet(workbook, locationRepository) }.doesNotThrowAnyException()
        }
    }

    @Nested
    inner class MappingRowToLocation {

        private val ignored: Cell = mock { on { it.stringCellValue } doReturn "ignored" }
        private val unsupportedLocationType: Cell = mock { on { it.stringCellValue } doReturn "bad location" }
        private val supportedLocationType: Cell = mock { on { it.stringCellValue } doReturn LocationType.CC.label }
        private val site: Cell = mock { on { it.stringCellValue } doReturn "site" }
        private val agency: Cell = mock { on { it.stringCellValue } doReturn "AGENCY_ID" }
        private val blankCell: Cell = mock { on { it.stringCellValue } doReturn "" }

        @Test
        internal fun `throws error for unsupported location type`() {
            assertThatThrownBy { spreadsheet.mapToLocation(listOf(ignored, unsupportedLocationType, site, agency)) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Unsupported location type: bad location")
        }

        @Test
        internal fun `court location type is mapped correctly`() {
            whenever(locationRepository.findByNomisAgencyId(any())).thenReturn(null)

            assertThat(spreadsheet.mapToLocation(listOf(ignored, supportedLocationType, site, agency)).locationType).isEqualTo(LocationType.CC)
        }

        @Test
        internal fun `throws error if agency id is blank`() {
            assertThatThrownBy { spreadsheet.mapToLocation(listOf(ignored, supportedLocationType, site, blankCell)) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("Agency id cannot be blank")
        }

        @Test
        internal fun `throws error if site name is blank`() {
            assertThatThrownBy { spreadsheet.mapToLocation(listOf(ignored, supportedLocationType, blankCell, agency)) }
                    .isInstanceOf(NullPointerException::class.java)
                    .hasMessage("Site name cannot be blank")
        }

        @Test
        internal fun `throws error if duplicate agency id`() {
            whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(location)

            assertThatThrownBy { spreadsheet.mapToLocation(listOf(ignored, supportedLocationType, site, agency)) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Agency id 'AGENCY_ID' already exists")
        }
    }

    @Nested
    inner class RecordingErrors {
        @Test
        internal fun `no errors by default`() {
            assertThat(spreadsheet.errors).isEmpty()
        }

        @Test
        internal fun `errors are recorded correctly`() {
            val row: Row = mock { on { it.rowNum } doReturn 1 }

            val exception = RuntimeException("something went wrong")

            spreadsheet.addError(LocationsSpreadsheet.Tab.COURT, row, exception)

            assertThat(spreadsheet.errors).containsOnly(LocationsSpreadsheetError(LocationsSpreadsheet.Tab.COURT , row.rowNum + 1, exception))
        }
    }

    @Test
    internal fun `locations spreadsheet is closed cleanly`() {
        spreadsheet.close()

        verify(workbook).close()
    }
}
