package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceAdjustmentRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.util.UUID

internal class AnnualPriceAdjustmentsServiceTest {

  private val priceAdjustmentRepository: PriceAdjustmentRepository = mock()
  private val priceRepository: PriceRepository = mock()
  private val timeSource = TimeSource { LocalDate.of(2020, 1, 1).atStartOfDay() }
  private val effectiveYear = EffectiveYear(timeSource)
  private val monitoringService: MonitoringService = mock()
  private val auditService: AuditService = mock()
  private val annualPriceAdjuster: AnnualPriceAdjuster =
    AnnualPriceAdjuster(priceRepository, priceAdjustmentRepository, auditService, timeSource)
  private val annualPriceAdjusterSpy: AnnualPriceAdjuster = mock { spy(annualPriceAdjuster) }

  @Test
  internal fun `price uplift for Serco`() {
    val lockId = fakeLockForFor(Supplier.SERCO)

    AnnualPriceAdjustmentsService(annualPriceAdjusterSpy, monitoringService, auditService, effectiveYear).uplift(
      Supplier.SERCO,
      2020,
      1.0
    )

    verify(annualPriceAdjusterSpy).uplift(eq(lockId), eq(Supplier.SERCO), eq(2020), eq(1.0))
  }

  @Test
  internal fun `price uplift for GEOAmey`() {
    val lockId = fakeLockForFor(Supplier.GEOAMEY)

    AnnualPriceAdjustmentsService(annualPriceAdjusterSpy, monitoringService, auditService, effectiveYear).uplift(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(annualPriceAdjusterSpy).uplift(eq(lockId), eq(Supplier.GEOAMEY), eq(2021), eq(2.0))
  }

  @Test
  internal fun `monitoring service captures failed price uplift`() {
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(PriceAdjustment(supplier = Supplier.GEOAMEY))
    whenever(annualPriceAdjusterSpy.uplift(any(), eq(Supplier.GEOAMEY), eq(2021), eq(2.0))).thenThrow(RuntimeException("something went wrong"))

    AnnualPriceAdjustmentsService(annualPriceAdjuster, monitoringService, auditService, effectiveYear).uplift(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(monitoringService).capture("Failed price uplift for GEOAMEY for effective year 2021 and multiplier 2.0.")
  }

  @Test
  internal fun `auditing service captures successful price uplift`() {
    fakeLockForFor(Supplier.GEOAMEY)
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(PriceAdjustment(supplier = Supplier.GEOAMEY))
    whenever(priceAdjustmentRepository.existsById(any())).thenReturn(true)

    AnnualPriceAdjustmentsService(annualPriceAdjuster, monitoringService, auditService, effectiveYear).uplift(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPLIFT,
        "_TERMINAL_",
        mapOf("supplier" to Supplier.GEOAMEY, "effective_year" to 2021, "multiplier" to 2.0)
      )
    )
  }

  @Test
  internal fun `cannot uplift years prior to the current effective year`() {
    assertThatThrownBy {
      AnnualPriceAdjustmentsService(annualPriceAdjuster, monitoringService, auditService, effectiveYear).uplift(
        Supplier.GEOAMEY,
        effectiveYear.current() - 1,
        2.0
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price uplifts cannot be before the current effective year ${effectiveYear.current()}.")
  }

  private fun fakeLockForFor(supplier: Supplier): UUID {
    val lockId = UUID.randomUUID()

    whenever(annualPriceAdjusterSpy.attemptLockForPriceAdjustment(supplier)).thenReturn(lockId)

    return lockId
  }
}
