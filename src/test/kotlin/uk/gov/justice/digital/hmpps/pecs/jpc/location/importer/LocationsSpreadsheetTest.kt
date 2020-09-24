package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

internal class LocationsSpreadsheetTest {

    // TODO - consider adding minimal test spreadsheets to the test resources

    private val ignored: Cell = mock { on { it.stringCellValue } doReturn "ignored" }
    private val unsupportedLocationType: Cell = mock { on { it.stringCellValue } doReturn "bad location" }
    private val supportedLocationType: Cell = mock { on { it.stringCellValue } doReturn LocationType.CC.label }
    private val site: Cell = mock { on { it.stringCellValue } doReturn "site" }
    private val agency: Cell = mock { on { it.stringCellValue } doReturn "agency" }
    private val workbook: Workbook = mock()
    private val spreadsheet: LocationsSpreadsheet = LocationsSpreadsheet(workbook)

    @Nested
    inner class MappingRowToLocation {
        @Test
        internal fun `throws error for unsupported location type`() {
            assertThatThrownBy { spreadsheet.mapToLocation(listOf(ignored, unsupportedLocationType, site, agency)) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .withFailMessage("Unsupported location type: ", unsupportedLocationType.stringCellValue)
        }

        @Test
        internal fun `court location type is mapped correctly`() {
            assertThat(spreadsheet.mapToLocation(listOf(ignored, supportedLocationType, site, agency)).locationType).isEqualTo(LocationType.CC)
        }
    }
}