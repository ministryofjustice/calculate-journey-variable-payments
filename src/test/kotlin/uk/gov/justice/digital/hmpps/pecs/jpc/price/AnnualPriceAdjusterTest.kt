package uk.gov.justice.digital.hmpps.pecs.jpc.price

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.time.LocalDateTime
import java.util.stream.Stream

internal class AnnualPriceAdjusterTest {

  private val priceAdjustmentRepository: PriceAdjustmentRepository = mock()
  private val priceRepository: PriceRepository = mock()
  private val timeSource = TimeSource { LocalDateTime.of(2021, 7, 22, 0, 0) }
  private val auditService: AuditService = mock()
  private val priceAdjuster = AnnualPriceAdjuster(priceRepository, priceAdjustmentRepository, auditService, timeSource)
  private val upliftCaptor = argumentCaptor<PriceAdjustment>()
  private val fromLocation: Location = mock()
  private val toLocation: Location = mock()

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
  fun `previous years prices are successfully uplifted for Serco`() {
    val previousYearPrice = Price(supplier = Supplier.SERCO, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10000, effectiveYear = 2019)

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.SERCO, 2019)).thenReturn(Stream.of(previousYearPrice))
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(any(), any(), any(), any())).thenReturn(null)

    priceAdjuster.uplift(Supplier.SERCO, 2020, 1.5, {}, { count -> assertThat(count).isEqualTo(1) })

    verify(priceAdjustmentRepository).saveAndFlush(upliftCaptor.capture())

    with(upliftCaptor.firstValue) {
      assertThat(supplier).isEqualTo(Supplier.SERCO)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
    }

    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.SERCO, 2019)
    verify(priceAdjustmentRepository).deleteBySupplier(Supplier.SERCO)
  }

  @Test
  fun `failed uplift for Serco`() {
    val exception = RuntimeException("something went wrong for Serco uplift")

    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenThrow(exception)

    priceAdjuster.uplift(Supplier.SERCO, 2020, 1.0, { thrown -> assertThat(thrown).isEqualTo(exception) }, {})

    verify(priceAdjustmentRepository).saveAndFlush(any())
    verifyZeroInteractions(priceRepository)
    verify(priceAdjustmentRepository).deleteBySupplier(Supplier.SERCO)
  }

  @Test
  fun `previous years prices are successfully uplifted for GEOAmey`() {
    val previousYearPrice = Price(supplier = Supplier.GEOAMEY, fromLocation = fromLocation, toLocation = toLocation, priceInPence = 10000, effectiveYear = 2020)

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2020)).thenReturn(Stream.of(previousYearPrice))

    priceAdjuster.uplift(Supplier.GEOAMEY, 2021, 2.0, {}, { count -> assertThat(count).isEqualTo(1) })

    verify(priceAdjustmentRepository).saveAndFlush(upliftCaptor.capture())

    with(upliftCaptor.firstValue) {
      assertThat(supplier).isEqualTo(Supplier.GEOAMEY)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
    }

    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2020)
    verify(priceAdjustmentRepository).deleteBySupplier(Supplier.GEOAMEY)
  }

  @Test
  fun `failed uplift for GEOAmey`() {
    val exception = RuntimeException("something went wrong for GEOAmey uplift")

    whenever(priceAdjustmentRepository.saveAndFlush(any())).thenThrow(exception)

    priceAdjuster.uplift(Supplier.GEOAMEY, 2020, 1.0, { thrown -> assertThat(thrown).isEqualTo(exception) }, {})

    verify(priceAdjustmentRepository).saveAndFlush(any())
    verifyZeroInteractions(priceRepository)
    verify(priceAdjustmentRepository).deleteBySupplier(Supplier.GEOAMEY)
  }
}
