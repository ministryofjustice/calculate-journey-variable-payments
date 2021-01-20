package uk.gov.justice.digital.hmpps.pecs.jpc.importer.location

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

internal class LocationsSpreadsheetTest {

  private val workbook: Workbook =
    XSSFWorkbook().apply { LocationsSpreadsheet.Tab.values().forEach { this.createSheet(it.label) } }
  private val location: Location = mock()
  private val locationRepository: LocationRepository = mock()
  private val spreadsheet: LocationsSpreadsheet = LocationsSpreadsheet(workbook, locationRepository)

  @Nested
  inner class Instantiation {
    @Test
    internal fun `fails instantiation if court and immigration tabs are missing the locations spreadsheet`() {
      val workbookWithMissingSheets = XSSFWorkbook().apply {
        this.createSheet(LocationsSpreadsheet.Tab.COURT.label)
        this.createSheet(LocationsSpreadsheet.Tab.IMMIGRATION.label)
      }

      assertThatThrownBy { LocationsSpreadsheet(workbookWithMissingSheets, locationRepository) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("The following tabs are missing from the locations spreadsheet: Hospitals, Other, Police, Prisons, Probation, STC&SCH")
    }

    @Test
    internal fun `succeeds instantiation when all tabs are present in the locations spreadsheet`() {
      assertThatCode { LocationsSpreadsheet(workbook, locationRepository) }.doesNotThrowAnyException()
    }
  }

  @Nested
  inner class MappingRowToLocation {

    private val row = workbook.getSheetAt(0).createRow(0).apply {
      this.createCell(0).setCellValue("ignored")
      this.createCell(1).setCellValue("Crown Court")
      this.createCell(2).setCellValue("Site")
      this.createCell(3).setCellValue("AGENCY_ID")
    }

    @Test
    internal fun `throws error for unsupported location type`() {
      row.getCell(1).setCellValue("bad location type")

      assertThatThrownBy { spreadsheet.toLocation(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Unsupported location type: BAD LOCATION TYPE")
    }

    @Test
    internal fun `court location type is mapped correctly`() {
      whenever(locationRepository.findByNomisAgencyId(any())).thenReturn(null)

      assertThat(spreadsheet.toLocation(row).locationType).isEqualTo(LocationType.CC)
    }

    @Test
    internal fun `throws error if agency id is blank`() {
      row.getCell(3).setCellValue("")

      assertThatThrownBy { spreadsheet.toLocation(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Agency id cannot be blank")
    }

    @Test
    internal fun `throws error if site name is blank`() {
      row.getCell(2).setCellValue("")

      assertThatThrownBy { spreadsheet.toLocation(row) }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Site name cannot be blank")
    }

    @Test
    internal fun `throws error if duplicate agency id`() {
      whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(location)

      assertThatThrownBy { spreadsheet.toLocation(row) }
        .isInstanceOf(RuntimeException::class.java)
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

      assertThat(spreadsheet.errors).containsOnly(
        LocationsSpreadsheetError(
          LocationsSpreadsheet.Tab.COURT,
          row.rowNum + 1,
          exception
        )
      )
    }
  }
}
