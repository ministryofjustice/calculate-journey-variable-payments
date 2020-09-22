package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.poi.ss.usermodel.Cell
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocationTypeTest {

    private val ignored: Cell = mock { on { it.stringCellValue } doReturn "ignored" }
    private val type: Cell = mock { on { it.stringCellValue } doReturn "type" }
    private val site: Cell = mock { on { it.stringCellValue } doReturn "site" }
    private val agency: Cell = mock { on { it.stringCellValue } doReturn "agency" }
    private val active: Cell = mock { on { it.stringCellValue } doReturn "Active" }
    private val inactive: Cell = mock { on { it.stringCellValue } doReturn "Inactive" }

    @Test
    fun `active locations (without column offset) are created`() {
        listOf(LocationType.HOSPITAL, LocationType.IMMIGRATION, LocationType.OTHER, LocationType.PRISON, LocationType.STCSCH).forEach {
            assertThat(it.active(listOf(ignored, type, site, agency, ignored, ignored, active))).isNotNull
        }
    }

    @Test
    fun `inactive locations (without column offset) is not created`() {
        listOf(LocationType.HOSPITAL, LocationType.IMMIGRATION, LocationType.OTHER, LocationType.PRISON, LocationType.STCSCH).forEach {
            assertThat(it.active(listOf(ignored, type, site, agency, ignored, ignored, inactive))).isNull()
        }
    }

    @Test
    fun `active police location (with column offset) is created`() {
        assertThat(LocationType.POLICE.active(listOf(ignored, type, site, agency, ignored, ignored, ignored, active))).isNotNull
    }

    @Test
    fun `inactive police location (with column offset) is not created`() {
        assertThat(LocationType.POLICE.active(listOf(ignored, type, site, agency, ignored, ignored, ignored, inactive))).isNull()
    }

    @Test
    fun `active court location (with column offset) is created`() {
        assertThat(LocationType.COURT.active(listOf(ignored, type, site, agency, ignored, ignored, ignored, ignored, active))).isNotNull
    }

    @Test
    fun `inactive court location (with column offset) is not created`() {
        assertThat(LocationType.COURT.active(listOf(ignored, type, site, agency, ignored, ignored, ignored, ignored, inactive))).isNull()
    }
}
