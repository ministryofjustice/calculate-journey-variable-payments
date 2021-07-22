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
import java.time.LocalDateTime

internal class PriceUplifterTest {

  private val supplierPriceUpliftRepository: SupplierPriceUpliftRepository = mock()
  private val priceRepository: PriceRepository = mock()
  private val timeSource = TimeSource { LocalDateTime.of(2021, 7, 22, 0, 0) }
  private val uplifter = PriceUplifter(priceRepository, supplierPriceUpliftRepository, timeSource)
  private val upliftCaptor = argumentCaptor<SupplierPriceUplift>()

  @Test
  fun `successful uplift for Serco`() {
    uplifter.uplift(Supplier.SERCO, 2020, 1.0, {}, { count -> assertThat(count).isEqualTo(-1) })

    verify(supplierPriceUpliftRepository).saveAndFlush(upliftCaptor.capture())

    with(upliftCaptor.firstValue) {
      assertThat(supplier).isEqualTo(Supplier.SERCO)
      assertThat(effectiveYear).isEqualTo(2020)
      assertThat(multiplier).isEqualTo(1.0)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
    }

    // TODO assert/verify activity with price repository

    verify(supplierPriceUpliftRepository).deleteBySupplier(Supplier.SERCO)
  }

  @Test
  fun `failed uplift for Serco`() {
    val exception = RuntimeException("something went wrong for Serco uplift")

    whenever(supplierPriceUpliftRepository.saveAndFlush(any())).thenThrow(exception)

    uplifter.uplift(Supplier.SERCO, 2020, 1.0, { thrown -> assertThat(thrown).isEqualTo(exception) }, {})

    verify(supplierPriceUpliftRepository).saveAndFlush(any())

    verifyZeroInteractions(priceRepository)

    verify(supplierPriceUpliftRepository).deleteBySupplier(Supplier.SERCO)
  }

  @Test
  fun `successful uplift for GEOAmey`() {
    uplifter.uplift(Supplier.GEOAMEY, 2021, 2.0, {}, { count -> assertThat(count).isEqualTo(-1) })

    verify(supplierPriceUpliftRepository).saveAndFlush(upliftCaptor.capture())

    with(upliftCaptor.firstValue) {
      assertThat(supplier).isEqualTo(Supplier.GEOAMEY)
      assertThat(effectiveYear).isEqualTo(2021)
      assertThat(multiplier).isEqualTo(2.0)
      assertThat(addedAt).isEqualTo(timeSource.dateTime())
    }

    // TODO assert/verify activity with price repository

    verify(supplierPriceUpliftRepository).deleteBySupplier(Supplier.GEOAMEY)
  }

  @Test
  fun `failed uplift for GEOAmey`() {
    val exception = RuntimeException("something went wrong for GEOAmey uplift")

    whenever(supplierPriceUpliftRepository.saveAndFlush(any())).thenThrow(exception)

    uplifter.uplift(Supplier.GEOAMEY, 2020, 1.0, { thrown -> assertThat(thrown).isEqualTo(exception) }, {})

    verify(supplierPriceUpliftRepository).saveAndFlush(any())

    verifyZeroInteractions(priceRepository)

    verify(supplierPriceUpliftRepository).deleteBySupplier(Supplier.GEOAMEY)
  }
}
