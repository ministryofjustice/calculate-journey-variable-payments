package uk.gov.justice.digital.hmpps.pecs.jpc.importer.price

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class PriceImporterTest {

  private val priceRepo: PriceRepository = mock()

  private val locationRepo: LocationRepository = mock()

  private val sercoPricesProvider: SercoPricesProvider = mock()

  private val geoameyPricesProvider: GeoameyPricesProvider = mock()

  private val auditService: AuditService = mock()

  private val auditCaptor = argumentCaptor<AuditableEvent>()

  private val import: PriceImporter = PriceImporter(priceRepo, sercoPricesProvider, geoameyPricesProvider, locationRepo, auditService)

  @Test
  internal fun `verify import interactions for serco`() {
    whenever(sercoPricesProvider.get()).thenReturn(priceSheetWithRow(1.0, "SERCO FROM", "SERCO TO", 100.00))

    val fromLocation = Location(LocationType.PR, "ID1", "SERCO FROM")
    val toLocation = Location(LocationType.CC, "ID2", "SERCO TO")
    val priceToAudit = Price(supplier = Supplier.SERCO, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10000, effectiveYear = 2020)

    whenever(priceRepo.save(any())).thenReturn(priceToAudit)
    whenever(locationRepo.findAll()).thenReturn(listOf(fromLocation, toLocation))

    import.import(Supplier.SERCO, 2019)

    verify(locationRepo).findAll()
    verify(sercoPricesProvider).get()
    verify(priceRepo, times(2)).count()
    verify(priceRepo).save(any())
    verify(auditService).create(auditCaptor.capture())

    with(auditCaptor.firstValue) {
      assertThat(type).isEqualTo(AuditEventType.JOURNEY_PRICE)
      assertThat(metadata).isEqualTo(PriceMetadata.new(priceToAudit))
      assertThat(username).isEqualTo("_TERMINAL_")
    }
  }

  @Test
  internal fun `verify import interactions for geoamey`() {
    whenever(geoameyPricesProvider.get()).thenReturn(priceSheetWithRow(2.0, "GEO FROM", "GEO TO", 101.00))

    val fromLocation = Location(LocationType.PR, "ID1", "GEO FROM")
    val toLocation = Location(LocationType.CC, "ID2", "GEO TO")
    val priceToAudit = Price(supplier = Supplier.GEOAMEY, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10100, effectiveYear = 2019)

    whenever(priceRepo.save(any())).thenReturn(priceToAudit)
    whenever(locationRepo.findAll()).thenReturn(listOf(fromLocation, toLocation))

    import.import(Supplier.GEOAMEY, 2019)

    verify(locationRepo).findAll()
    verify(geoameyPricesProvider).get()
    verify(priceRepo, times(2)).count()
    verify(priceRepo).save(any())
    verify(auditService).create(auditCaptor.capture())

    with(auditCaptor.firstValue) {
      assertThat(type).isEqualTo(AuditEventType.JOURNEY_PRICE)
      assertThat(metadata).isEqualTo(PriceMetadata.new(priceToAudit))
      assertThat(username).isEqualTo("_TERMINAL_")
    }
  }

  @Test
  internal fun `import fails with runtime exception for unsupported supplier`() {
    assertThatThrownBy { import.import(Supplier.UNKNOWN, 2020) }.isInstanceOf(RuntimeException::class.java)
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
