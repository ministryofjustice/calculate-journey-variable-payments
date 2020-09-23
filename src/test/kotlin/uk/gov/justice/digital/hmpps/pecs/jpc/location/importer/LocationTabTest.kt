package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.poi.ss.usermodel.Cell
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

class LocationTabTest {

    private val ignored: Cell = mock { on { it.stringCellValue } doReturn "ignored" }
    private val unsupportedLocationType: Cell = mock { on { it.stringCellValue } doReturn "bad location" }
    private val supportedLocationType: Cell = mock { on { it.stringCellValue } doReturn LocationType.CC.label }
    private val site: Cell = mock { on { it.stringCellValue } doReturn "site" }
    private val agency: Cell = mock { on { it.stringCellValue } doReturn "agency" }

    @Test
    fun `throws error for unsupported location type`() {
        assertThatThrownBy { LocationTab.COURT.map(listOf(ignored, unsupportedLocationType, site, agency)) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .withFailMessage("Unsupported location type: ", unsupportedLocationType.stringCellValue)
    }

    @Test
    fun `court location type is mapped correctly`() {
        assertThat(LocationTab.COURT.map(listOf(ignored, supportedLocationType, site, agency)).locationType).isEqualTo(LocationType.CC)
    }
}
