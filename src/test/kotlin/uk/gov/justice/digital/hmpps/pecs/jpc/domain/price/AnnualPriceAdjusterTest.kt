package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream

internal class AnnualPriceAdjusterTest {

  private val priceAdjustmentRepository: PriceAdjustmentRepository = mock()
  private val priceRepository: PriceRepository = mock()
  private val timeSource = TimeSource { LocalDateTime.of(2021, 7, 22, 0, 0) }
  private val auditService: AuditService = mock()
  private val priceAdjuster = AnnualPriceAdjuster(priceRepository, priceAdjustmentRepository, auditService, timeSource)
  private val priceAdjusterCaptor = argumentCaptor<PriceAdjustment>()
  private val fromLocation: Location = mock { on { nomisAgencyId } doReturn "from_agency" }
  private val toLocation: Location = mock { on { nomisAgencyId } doReturn "to_agency" }

  @Test
  fun `adjustment is not in progress for supplier`() {
    whenever(priceAdjustmentRepository.existsPriceAdjustmentBySupplier(Supplier.SERCO)).thenReturn(false)

    assertThat(priceAdjuster.isInProgressFor(Supplier.SERCO))
  }

  @Test
  fun `adjustment is in progress for supplier`() {
    whenever(priceAdjustmentRepository.existsPriceAdjustmentBySupplier(Supplier.GEOAMEY)).thenReturn(true)

    assertThat(priceAdjuster.isInProgressFor(Supplier.GEOAMEY))
  }

  @Test
  fun `attempted lock for GEOAmey adjustment is successful`() {
    val expectedPriceAdjustment = PriceAdjustment(supplier = Supplier.GEOAMEY, addedAt = timeSource.dateTime(), multiplier = 1.5, effectiveYear = 2020)
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(expectedPriceAdjustment)

    val actualLockId = priceAdjuster.attemptLockForPriceAdjustment(Supplier.GEOAMEY, 1.5, 2020)

    verify(priceAdjustmentRepository).saveAndFlush(priceAdjusterCaptor.capture())

    with(priceAdjusterCaptor.firstValue) {
      assertThat(supplier).isEqualTo(uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier.GEOAMEY)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
      assertThat(multiplier).isEqualTo(1.5)
      assertThat(effectiveYear).isEqualTo(2020)
    }

    assertThat(actualLockId).isEqualTo(expectedPriceAdjustment.id)
  }

  @Test
  fun `attempted lock required for Serco adjustment is successful`() {
    val expectedPriceAdjustment = PriceAdjustment(supplier = Supplier.SERCO, addedAt = timeSource.dateTime(), multiplier = 2.0, effectiveYear = 2021)
    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenReturn(expectedPriceAdjustment)

    val actualLockId = priceAdjuster.attemptLockForPriceAdjustment(Supplier.SERCO, 2.0, 2021)

    verify(priceAdjustmentRepository).saveAndFlush(priceAdjusterCaptor.capture())

    with(priceAdjusterCaptor.firstValue) {
      assertThat(supplier).isEqualTo(uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier.SERCO)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
      assertThat(multiplier).isEqualTo(2.0)
      assertThat(effectiveYear).isEqualTo(2021)
    }

    assertThat(actualLockId).isEqualTo(expectedPriceAdjustment.id)
  }

  @Test
  fun `previous years prices are successfully adjusted for Serco`() {
    val lockId = fakeLock()

    val previousYearPrice = Price(supplier = Supplier.SERCO, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10000, effectiveYear = 2019)

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.SERCO, 2019)).thenReturn(Stream.of(previousYearPrice))
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(any(), any(), any(), any())).thenReturn(null)

    val adjusted = priceAdjuster.adjust(lockId, Supplier.SERCO, 2020, 1.5)

    assertThat(adjusted).isEqualTo(1)

    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.SERCO, 2019)
  }

  @Test
  fun `failed adjustment for Serco due to price adjustment lock not being in place`() {
    val lockId = fakeLock(false)

    assertThatThrownBy { priceAdjuster.adjust(lockId, Supplier.SERCO, 2020, 1.0) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Unable to upflift lock is not present for SERCO.")

    verifyZeroInteractions(priceRepository)
  }

  @Test
  fun `previous years prices are successfully adjusted for GEOAmey`() {
    val lockId = fakeLock()

    val previousYearPrice = Price(supplier = Supplier.GEOAMEY, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10000, effectiveYear = 2020)

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2020)).thenReturn(Stream.of(previousYearPrice))

    val adjusted = priceAdjuster.adjust(lockId, Supplier.GEOAMEY, 2021, 2.0)

    assertThat(adjusted).isEqualTo(1)

    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2020)
  }

  @Test
  fun `failed adjustment for GEOAmey due to price adjustment lock not being in place`() {
    val lockId = fakeLock(false)

    assertThatThrownBy { priceAdjuster.adjust(lockId, Supplier.GEOAMEY, 2020, 1.0) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Unable to upflift lock is not present for GEOAMEY.")

    verify(priceAdjustmentRepository).existsById(lockId)
    verifyZeroInteractions(priceRepository)
  }

  private fun fakeLock(present: Boolean = true): UUID {
    val lockId = UUID.randomUUID()

    whenever(priceAdjustmentRepository.existsById(lockId)).thenReturn(present)

    return lockId
  }
}
