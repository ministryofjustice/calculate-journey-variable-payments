package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime

internal class BulkPriceUpdatesServiceTest {

  private val priceRepository: PriceRepository = mock()
  private val fromLocation: Location = mock()
  private val toLocation: Location = mock()
  private val sercoPrice2020: Price = Price(
    supplier = Supplier.SERCO,
    fromLocation = fromLocation,
    toLocation = toLocation,
    priceInPence = 1000,
    effectiveYear = 2020
  )
  private val timeSourceEffectiveYear2020: TimeSource = TimeSource { LocalDateTime.of(2020, 12, 1, 0, 0) }
  private val timeSourceEffectiveYear2021: TimeSource = TimeSource { LocalDateTime.of(2021, 12, 1, 0, 0) }

  @Test
  internal fun `bulk price update for Serco next effective year 2021`() {
    whenever(priceRepository.findBySupplierAndEffectiveYear(Supplier.SERCO, 2020)).thenReturn(listOf(sercoPrice2020))

    val service = BulkPriceUpdatesService(priceRepository, timeSourceEffectiveYear2020)

    service.bulkPriceUpdate(Supplier.SERCO, 1.5)

    verify(priceRepository).deleteBySupplierAndEffectiveYear(Supplier.SERCO, 2021)
    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.SERCO, 2020)
    verify(priceRepository).save(any())
  }

  @Test
  internal fun `bulk price update for Geoamey next effective year 2022`() {
    val service = BulkPriceUpdatesService(priceRepository, timeSourceEffectiveYear2021)

    service.bulkPriceUpdate(Supplier.GEOAMEY, 1.5)

    verify(priceRepository).deleteBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2022)
    verify(priceRepository).findBySupplierAndEffectiveYear(Supplier.GEOAMEY, 2021)
  }
}
