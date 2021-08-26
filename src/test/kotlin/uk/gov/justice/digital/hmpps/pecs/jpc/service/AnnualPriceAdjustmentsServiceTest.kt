package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceAdjustmentRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
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
  private val jobRunner = JobRunner { _, job -> job() }

  @Test
  internal fun `price adjustment for Serco`() {
    val lockId = fakeLockForFor(Supplier.SERCO, 1.0, 2020)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.SERCO,
      2020,
      1.0
    )

    verify(annualPriceAdjusterSpy).adjust(eq(lockId), eq(Supplier.SERCO), eq(2020), eq(1.0))
  }

  @Test
  internal fun `price adjustment for GEOAmey`() {
    val lockId = fakeLockForFor(Supplier.GEOAMEY, 2.0, 2021)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(annualPriceAdjusterSpy).adjust(eq(lockId), eq(Supplier.GEOAMEY), eq(2021), eq(2.0))
  }

  @Test
  internal fun `monitoring service captures failed price adjustment`() {
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
      PriceAdjustment(
        supplier = Supplier.GEOAMEY,
        multiplier = 1.0,
        effectiveYear = 2021
      )
    )
    whenever(
      annualPriceAdjusterSpy.adjust(
        any(),
        eq(Supplier.GEOAMEY),
        eq(2021),
        eq(2.0)
      )
    ).thenThrow(RuntimeException("something went wrong"))

    AnnualPriceAdjustmentsService(
      annualPriceAdjuster,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(monitoringService).capture("Failed price adjustment for GEOAMEY for effective year 2021 and multiplier 2.0.")
  }

  @Test
  internal fun `auditing service captures successful price adjustment`() {
    fakeLockForFor(Supplier.GEOAMEY, 2.0, 2021)
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
      PriceAdjustment(
        supplier = Supplier.GEOAMEY,
        multiplier = 2.0,
        effectiveYear = 2021
      )
    )
    whenever(priceAdjustmentRepository.existsById(any())).thenReturn(true)

    AnnualPriceAdjustmentsService(
      annualPriceAdjuster,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      2.0
    )

    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
        "_TERMINAL_",
        mapOf("supplier" to Supplier.GEOAMEY, "effective_year" to 2021, "multiplier" to 2.0)
      )
    )
  }

  @Test
  internal fun `cannot adjust years prior to the current effective year`() {
    assertThatThrownBy {
      AnnualPriceAdjustmentsService(
        annualPriceAdjuster,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner
      ).adjust(
        Supplier.GEOAMEY,
        effectiveYear.current() - 1,
        2.0
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price adjustments cannot be before the current effective year ${effectiveYear.current()}.")
  }

  private fun fakeLockForFor(supplier: Supplier, multiplier: Double, effectiveYear: Int): UUID {
    val lockId = UUID.randomUUID()

    whenever(annualPriceAdjusterSpy.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)).thenReturn(
      lockId
    )

    return lockId
  }
}
