package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AnnualPriceAdjustmentMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceAdjustmentRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.util.UUID

@ExtendWith(FakeAuthentication::class)
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
  private lateinit var authentication: Authentication

  @BeforeEach
  fun before() {
    authentication = SecurityContextHolder.getContext().authentication
  }

  @Test
  internal fun `price adjustment for Serco`() {
    val lockId = fakeLockForFor(Supplier.SERCO, AdjustmentMultiplier(1.0.toBigDecimal()), 2020)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.SERCO,
      2020,
      AdjustmentMultiplier(1.0.toBigDecimal()),
      authentication,
      "some details"
    )

    verify(annualPriceAdjusterSpy).adjust(eq(lockId), eq(Supplier.SERCO), eq(2020), eq(AdjustmentMultiplier(1.0.toBigDecimal())))
  }

  @Test
  internal fun `price adjustment for GEOAmey`() {
    val lockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      AdjustmentMultiplier(2.0.toBigDecimal()),
      authentication,
      "some details"
    )

    verify(annualPriceAdjusterSpy).adjust(eq(lockId), eq(Supplier.GEOAMEY), eq(2021), eq(AdjustmentMultiplier(2.0.toBigDecimal())))
  }

  @Test
  internal fun `monitoring service captures failed price adjustment`() {
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
      PriceAdjustment(
        supplier = Supplier.GEOAMEY,
        multiplier = 1.0.toBigDecimal(),
        effectiveYear = 2021
      )
    )
    whenever(
      annualPriceAdjusterSpy.adjust(
        any(),
        eq(Supplier.GEOAMEY),
        eq(2021),
        eq(AdjustmentMultiplier(2.0.toBigDecimal()))
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
      AdjustmentMultiplier(2.0.toBigDecimal()),
      authentication,
      "some details"
    )

    verify(monitoringService).capture("Failed price adjustment for GEOAMEY for effective year 2021 and multiplier 2.0.")
  }

  @Test
  internal fun `auditing service captures successful price adjustment`() {
    fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
      PriceAdjustment(
        supplier = Supplier.GEOAMEY,
        multiplier = 2.0.toBigDecimal(),
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
      AdjustmentMultiplier(2.0.toBigDecimal()),
      authentication,
      "some audit details"
    )

    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
        authentication.name,
        AnnualPriceAdjustmentMetadata(Supplier.GEOAMEY, 2021, 2.0.toBigDecimal(), "some audit details")
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
        AdjustmentMultiplier(2.0.toBigDecimal()),
        authentication,
        "some details"
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price adjustments cannot be before the current effective year ${effectiveYear.current()}.")
  }

  @Test
  internal fun `max allowed price adjustment for GEOAmey`() {
    val lockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(9.99.toBigDecimal()), 2021)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      AdjustmentMultiplier(9.99.toBigDecimal()),
      authentication,
      "some details"
    )

    verify(annualPriceAdjusterSpy).adjust(eq(lockId), eq(Supplier.GEOAMEY), eq(2021), eq(AdjustmentMultiplier(9.99.toBigDecimal())))
  }

  private fun fakeLockForFor(supplier: Supplier, multiplier: AdjustmentMultiplier, effectiveYear: Int): UUID {
    val lockId = UUID.randomUUID()

    whenever(annualPriceAdjusterSpy.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)).thenReturn(
      lockId
    )

    return lockId
  }
}
