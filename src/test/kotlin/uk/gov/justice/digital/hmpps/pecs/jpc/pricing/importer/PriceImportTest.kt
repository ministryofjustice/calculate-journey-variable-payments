package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import com.nhaarman.mockitokotlin2.*
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

internal class PriceImportTest {

    private val priceRepo: PriceRepository = mock()

    private val locationRepo: LocationRepository = mock()

    private val sercoPricesProvider: SercoPricesProvider = mock()

    private val geoamyPricesProvider: GeoamyPricesProvider = mock()

    private val import: PriceImporter = PriceImporter(priceRepo, sercoPricesProvider, geoamyPricesProvider, locationRepo)

    @Test
    internal fun `verify import interactions for serco`() {
        whenever(sercoPricesProvider.get()).thenReturn(priceSheetWithRow(1.0, "SERCO FROM", "SERCO TO", 100.00))
        val fromLocation = Location(LocationType.PR, "ID1", "SERCO FROM")
        val toLocation = Location(LocationType.CC, "ID2", "SERCO TO")
        whenever(locationRepo.findAll()).thenReturn(listOf(fromLocation, toLocation))

        import.import(Supplier.SERCO)

        verify(locationRepo).findAll()
        verify(sercoPricesProvider).get()
        verify(priceRepo).deleteAll()
        verify(priceRepo, times(2)).count()
        verify(priceRepo).save(any())
    }

    @Test
    internal fun `verify import interactions for geoamey`() {
        whenever(geoamyPricesProvider.get()).thenReturn(priceSheetWithRow(2.0, "GEO FROM", "GEO TO", 101.00))
        val fromLocation = Location(LocationType.PR, "ID1", "GEO FROM")
        val toLocation = Location(LocationType.CC, "ID2", "GEO TO")
        whenever(locationRepo.findAll()).thenReturn(listOf(fromLocation, toLocation))

        import.import(Supplier.GEOAMEY)

        verify(locationRepo).findAll()
        verify(geoamyPricesProvider).get()
        verify(priceRepo).deleteAll()
        verify(priceRepo, times(2)).count()
        verify(priceRepo).save(any())
    }

    private fun priceSheetWithRow(journeyId: Double, fromSite: String, toSite: String, price: Double): InputStream {
        val workbook: Workbook = XSSFWorkbook().apply {
            this.createSheet().apply {
                this.createRow(0)
                this.createRow(1).apply {
                    this.createCell(0).setCellValue(journeyId)
                    this.createCell(1).setCellValue(fromSite)
                    this.createCell(2).setCellValue(toSite)
                    this.createCell(3).setCellValue(price)
                }
            }
        }

        val outputStream = ByteArrayOutputStream()

        workbook.use { it.write(outputStream) }

        return ByteArrayInputStream(outputStream.toByteArray())
    }
}
