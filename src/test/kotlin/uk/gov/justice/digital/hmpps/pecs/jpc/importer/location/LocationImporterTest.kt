package uk.gov.justice.digital.hmpps.pecs.jpc.importer.location

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class LocationImporterTest {

  private val locationRepo: LocationRepository = mock()
  private val priceRepo: PriceRepository = mock()

  private val schedule34LocationsProvider: Schedule34LocationsProvider =
    mock { on { get() } doReturn locationSheetWithCrownCourt() }

  private val importer: LocationsImporter = LocationsImporter(locationRepo, priceRepo, schedule34LocationsProvider)

  @Test
  internal fun `verify import interactions`() {
    importer.import()

    verify(schedule34LocationsProvider).get()
    verify(priceRepo).deleteAll()
    verify(locationRepo).deleteAll()
    verify(locationRepo, times(2)).count()
    verify(locationRepo).save(any())
  }

  private fun locationSheetWithCrownCourt(): InputStream {
    val workbook: Workbook = XSSFWorkbook().apply {
      LocationsSpreadsheet.Tab.values().forEach {
        this.createSheet(it.label).apply {
          if (it == LocationsSpreadsheet.Tab.COURT) {
            this.createRow(0)
            this.createRow(1).apply {
              this.createCell(0).setCellValue("ignored")
              this.createCell(1).setCellValue("Crown Court")
              this.createCell(2).setCellValue("Site")
              this.createCell(3).setCellValue("AGENCY_ID")
            }
          }
        }
      }
    }

    val outputStream = ByteArrayOutputStream()

    workbook.use { it.write(outputStream) }

    return ByteArrayInputStream(outputStream.toByteArray())
  }
}
