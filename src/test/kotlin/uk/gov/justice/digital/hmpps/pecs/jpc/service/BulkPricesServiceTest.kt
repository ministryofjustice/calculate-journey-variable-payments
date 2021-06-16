package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime
import java.util.stream.Stream

internal class BulkPricesServiceTest {

  private val auditService: AuditService = mock()
  private val priceRepository: PriceRepository = mock()
  private val fromLocation: Location = mock()
  private val toLocation: Location = mock()
  private val timeSourceEffectiveYear2020: TimeSource = TimeSource { LocalDateTime.of(2020, 12, 1, 0, 0) }
  private val timeSourceEffectiveYear2021: TimeSource = TimeSource { LocalDateTime.of(2021, 12, 1, 0, 0) }
  private val priceCaptor = argumentCaptor<Price>()

  @Test
  internal fun `bulk price updates for Serco next effective year 2021`() {
    val prices = Stream.of(price(Supplier.SERCO, 1000, 2020), price(Supplier.SERCO, 2000, 2020))

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.SERCO, 2020)).thenReturn(prices)

    val service = BulkPricesService(priceRepository, timeSourceEffectiveYear2020, auditService)

    service.addNextYearsPrices(Supplier.SERCO, 1.5)

    verify(priceRepository).deleteBySupplierAndEffectiveYear(Supplier.SERCO, 2021)
    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.SERCO, 2020)
    verify(priceRepository, times(2)).save(priceCaptor.capture())
    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        "_TERMINAL_",
        mapOf("supplier" to Supplier.SERCO, "multiplier" to 1.5)
      )
    )
    assertOnSupplierPriceAndEffectiveYear(priceCaptor.firstValue, Supplier.SERCO, Money(1500), 2021)
    assertOnSupplierPriceAndEffectiveYear(priceCaptor.secondValue, Supplier.SERCO, Money(3000), 2021)
  }

  @Test
  internal fun `bulk price updates for Geoamey next effective year 2022`() {
    val prices = Stream.of(price(Supplier.GEOAMEY, 1500, 2021), price(Supplier.GEOAMEY, 2000, 2021))

    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2021)).thenReturn(prices)

    val service = BulkPricesService(priceRepository, timeSourceEffectiveYear2021, auditService)

    service.addNextYearsPrices(Supplier.GEOAMEY, 2.0)

    verify(priceRepository).deleteBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2022)
    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2021)
    verify(priceRepository, times(2)).save(priceCaptor.capture())
    verify(auditService).create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        "_TERMINAL_",
        mapOf("supplier" to Supplier.GEOAMEY, "multiplier" to 2.0)
      )
    )
    assertOnSupplierPriceAndEffectiveYear(priceCaptor.firstValue, Supplier.GEOAMEY, Money(3000), 2022)
    assertOnSupplierPriceAndEffectiveYear(priceCaptor.secondValue, Supplier.GEOAMEY, Money(4000), 2022)
  }

  private fun price(supplier: Supplier, priceInPence: Int, effectiveYear: Int) =
    Price(
      supplier = supplier,
      fromLocation = fromLocation,
      toLocation = toLocation,
      priceInPence = priceInPence,
      effectiveYear = effectiveYear
    )

  private fun assertOnSupplierPriceAndEffectiveYear(
    price: Price,
    expectedSupplier: Supplier,
    expectedPrice: Money,
    expectedEffectiveYear: Int
  ) {
    assertThat(price.supplier).isEqualTo(expectedSupplier)
    assertThat(price.price()).isEqualTo(expectedPrice)
    assertThat(price.effectiveYear).isEqualTo(expectedEffectiveYear)
  }
}
