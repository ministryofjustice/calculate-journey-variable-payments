package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceUplifter
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.SupplierPriceUpliftRepository
import java.time.LocalDateTime

internal class PriceUpliftServiceTest {

  private val supplierPriceUpliftRepository: SupplierPriceUpliftRepository = mock()
  private val priceRepository: PriceRepository = mock()
  private val timeSource = TimeSource { LocalDateTime.now() }
  private val monitoringService: MonitoringService = mock()
  private val priceUplifter: PriceUplifter = PriceUplifter(priceRepository, supplierPriceUpliftRepository, timeSource)
  private val priceUplifterSpy: PriceUplifter = mock { spy(priceUplifter) }
  private val auditService: AuditService = mock()

  @Test
  internal fun `price uplift for Serco`() {
    PriceUpliftService(priceUplifterSpy, monitoringService, auditService).uplift(Supplier.SERCO, 2020, 1.0)

    verify(priceUplifterSpy).uplift(eq(Supplier.SERCO), eq(2020), eq(1.0), any(), any())
  }

  @Test
  internal fun `price uplift for GEOAmey`() {
    PriceUpliftService(priceUplifterSpy, monitoringService, auditService).uplift(Supplier.GEOAMEY, 2021, 2.0)

    verify(priceUplifterSpy).uplift(eq(Supplier.GEOAMEY), eq(2021), eq(2.0), any(), any())
  }

  @Test
  internal fun `monitoring service captures failed price uplift`() {
    whenever(supplierPriceUpliftRepository.saveAndFlush(any())).thenThrow(RuntimeException("something went wrong"))

    PriceUpliftService(priceUplifter, monitoringService, auditService).uplift(Supplier.GEOAMEY, 2021, 2.0)

    verify(monitoringService).capture("Failed price uplift for GEOAMEY for effective year 2021 and multiplier 2.0.")
  }

  @Test
  internal fun `auditing service captures successful price uplift`() {
    PriceUpliftService(priceUplifter, monitoringService, auditService).uplift(Supplier.GEOAMEY, 2021, 2.0)

    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        "_TERMINAL_",
        mapOf("supplier" to Supplier.GEOAMEY, "effective_year" to 2021, "multiplier" to 2.0)
      )
    )
  }
}
