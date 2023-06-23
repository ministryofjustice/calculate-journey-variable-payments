package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.FakeAuthentication
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JobRunner
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
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

  @Nested
  inner class InflationaryAdjustments {
    @Test
    internal fun `price adjustment for Serco`() {
      val lockId = fakeLockForFor(Supplier.SERCO, AdjustmentMultiplier(1.0.toBigDecimal()), 2020)

      AnnualPriceAdjustmentsService(
        annualPriceAdjusterSpy,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.SERCO,
        2020,
        AdjustmentMultiplier(1.0.toBigDecimal()),
        null,
        authentication,
        "some details",
      )

      verify(annualPriceAdjusterSpy).inflationary(
        eq(lockId),
        eq(Supplier.SERCO),
        eq(2020),
        eq(AdjustmentMultiplier(1.0.toBigDecimal())),
      )
    }

    @Test
    internal fun `price adjustment for GEOAmey`() {
      val lockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)

      AnnualPriceAdjustmentsService(
        annualPriceAdjusterSpy,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.GEOAMEY,
        2021,
        AdjustmentMultiplier(2.0.toBigDecimal()),
        null,
        authentication,
        "some details",
      )

      verify(annualPriceAdjusterSpy).inflationary(
        eq(lockId),
        eq(Supplier.GEOAMEY),
        eq(2021),
        eq(AdjustmentMultiplier(2.0.toBigDecimal())),
      )
    }

    @Test
    internal fun `monitoring service captures failed price adjustment`() {
      whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(mock())

      whenever(
        annualPriceAdjusterSpy.inflationary(
          any(),
          eq(Supplier.GEOAMEY),
          eq(2021),
          eq(AdjustmentMultiplier(2.0.toBigDecimal())),
        ),
      ).thenThrow(RuntimeException("something went wrong"))

      AnnualPriceAdjustmentsService(
        annualPriceAdjuster,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.GEOAMEY,
        2021,
        AdjustmentMultiplier(2.0.toBigDecimal()),
        null,
        authentication,
        "some details",
      )

      verify(monitoringService).capture("Failed INFLATION price adjustment for GEOAMEY for effective year 2021 and multiplier 2.0.")
    }

    @Test
    internal fun `auditing service captures successful price adjustment`() {
      fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)
      whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
        PriceAdjustment(
          supplier = Supplier.GEOAMEY,
          multiplier = 2.0.toBigDecimal(),
          effectiveYear = 2021,
        ),
      )

      whenever(priceAdjustmentRepository.existsById(any())).thenReturn(true)

      AnnualPriceAdjustmentsService(
        annualPriceAdjuster,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.GEOAMEY,
        2021,
        AdjustmentMultiplier(2.0.toBigDecimal()),
        null,
        authentication,
        "some audit details",
      )

      verify(auditService).create(
        AuditableEvent(
          AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
          authentication.name,
          AnnualPriceAdjustmentMetadata(Supplier.GEOAMEY, 2021, 2.0.toBigDecimal(), "some audit details"),
        ),
      )
    }

    @Test
    internal fun `cannot adjust years prior to the previous effective year`() {
      assertThatThrownBy {
        AnnualPriceAdjustmentsService(
          annualPriceAdjuster,
          monitoringService,
          auditService,
          effectiveYear,
          jobRunner,
        ).adjust(
          Supplier.GEOAMEY,
          effectiveYear.current() - 2,
          AdjustmentMultiplier(2.0.toBigDecimal()),
          null,
          authentication,
          "some details",
        )
      }.isInstanceOf(RuntimeException::class.java)
        .hasMessage("Price adjustments cannot be before the previous effective year ${effectiveYear.previous()}.")
    }
  }

  @Nested
  inner class VolumetricAdjustments {
    @Test
    internal fun `price adjustment for Serco`() {
      val lockId = fakeLockForFor(Supplier.SERCO, AdjustmentMultiplier(2.0.toBigDecimal()), 2020)

      AnnualPriceAdjustmentsService(
        annualPriceAdjusterSpy,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.SERCO,
        2020,
        AdjustmentMultiplier(1.0.toBigDecimal()),
        AdjustmentMultiplier(2.0.toBigDecimal()),
        authentication,
        "some details",
      )

      verify(annualPriceAdjusterSpy).volumetric(
        eq(lockId),
        eq(Supplier.SERCO),
        eq(2020),
        eq(AdjustmentMultiplier(2.0.toBigDecimal())),
      )
    }

    @Test
    internal fun `price adjustment for GEOAmey`() {
      val lockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)

      AnnualPriceAdjustmentsService(
        annualPriceAdjusterSpy,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.GEOAMEY,
        2021,
        AdjustmentMultiplier(1.0.toBigDecimal()),
        AdjustmentMultiplier(2.0.toBigDecimal()),
        authentication,
        "some details",
      )

      verify(annualPriceAdjusterSpy).volumetric(
        eq(lockId),
        eq(Supplier.GEOAMEY),
        eq(2021),
        eq(AdjustmentMultiplier(2.0.toBigDecimal())),
      )
    }

    @Test
    internal fun `auditing service captures successful price adjustment`() {
      fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)
      whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(
        PriceAdjustment(
          supplier = Supplier.GEOAMEY,
          multiplier = 2.0.toBigDecimal(),
          effectiveYear = 2021,
        ),
      )
      whenever(priceAdjustmentRepository.existsById(any())).thenReturn(true)

      AnnualPriceAdjustmentsService(
        annualPriceAdjuster,
        monitoringService,
        auditService,
        effectiveYear,
        jobRunner,
      ).adjust(
        Supplier.GEOAMEY,
        2021,
        AdjustmentMultiplier(2.0.toBigDecimal()),
        null,
        authentication,
        "some audit details",
      )

      verify(auditService).create(
        AuditableEvent(
          AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
          authentication.name,
          AnnualPriceAdjustmentMetadata(Supplier.GEOAMEY, 2021, 2.0.toBigDecimal(), "some audit details"),
        ),
      )
    }

    @Test
    internal fun `cannot adjust years prior to the previous effective year`() {
      assertThatThrownBy {
        AnnualPriceAdjustmentsService(
          annualPriceAdjuster,
          monitoringService,
          auditService,
          effectiveYear,
          jobRunner,
        ).adjust(
          Supplier.GEOAMEY,
          effectiveYear.current() - 2,
          AdjustmentMultiplier(2.0.toBigDecimal()),
          null,
          authentication,
          "some details",
        )
      }.isInstanceOf(RuntimeException::class.java)
        .hasMessage("Price adjustments cannot be before the previous effective year ${effectiveYear.previous()}.")
    }
  }

  @Test
  internal fun `price adjustments for GEOAmey`() {
    val inflationLockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(2.0.toBigDecimal()), 2021)
    val volumetricLockId = fakeLockForFor(Supplier.GEOAMEY, AdjustmentMultiplier(3.0.toBigDecimal()), 2021)

    AnnualPriceAdjustmentsService(
      annualPriceAdjusterSpy,
      monitoringService,
      auditService,
      effectiveYear,
      jobRunner,
    ).adjust(
      Supplier.GEOAMEY,
      2021,
      AdjustmentMultiplier(2.0.toBigDecimal()),
      AdjustmentMultiplier(3.0.toBigDecimal()),
      authentication,
      "some details",
    )

    verify(annualPriceAdjusterSpy).inflationary(
      eq(inflationLockId),
      eq(Supplier.GEOAMEY),
      eq(2021),
      eq(AdjustmentMultiplier(2.0.toBigDecimal())),
    )
    verify(annualPriceAdjusterSpy).volumetric(
      eq(volumetricLockId),
      eq(Supplier.GEOAMEY),
      eq(2021),
      eq(AdjustmentMultiplier(3.0.toBigDecimal())),
    )
  }

  private fun fakeLockForFor(supplier: Supplier, multiplier: AdjustmentMultiplier, effectiveYear: Int): UUID {
    val lockId = UUID.randomUUID()

    whenever(annualPriceAdjusterSpy.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)).thenReturn(
      lockId,
    )

    return lockId
  }
}
