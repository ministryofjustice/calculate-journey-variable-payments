package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class SupplierPricingServiceTest {

  private val priceInPence: Int = 10024
  private val locationRepository: LocationRepository = mock()
  private val fromLocation: Location = mock { on { siteName } doReturn "from site" }
  private val toLocation: Location = mock { on { siteName } doReturn "to site" }
  private val priceRepository: PriceRepository = mock()
  private val price: Price = mock { on { priceInPence } doReturn priceInPence }
  private val service: SupplierPricingService = SupplierPricingService(locationRepository, priceRepository)
  private val priceCaptor = argumentCaptor<Price>()

  @Test
  internal fun `site names returned for new pricing`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)).thenReturn(null)

    val result = service.getSiteNamesForPricing(Supplier.SERCO, "from", "to")

    assertThat(result).isEqualTo(Pair("from site", "to site"))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
  }

  @Test
  internal fun `add new price for supplier`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)

    service.addPriceForSupplier(Supplier.SERCO, "from", "to", Money.valueOf(100.24))

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).save(priceCaptor.capture())

    assertThat(priceCaptor.firstValue.priceInPence).isEqualTo(10024)
  }

  @Test
  internal fun `existing site names and price returned`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)).thenReturn(price)

    val result = service.getExistingSiteNamesAndPrice(Supplier.SERCO, "from", "to")

    assertThat(result).isEqualTo(Triple("from site", "to site", Money.valueOf(100.24)))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
  }

  @Test
  internal fun `update existing price for supplier`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)).thenReturn(price)

    service.updatePriceForSupplier(Supplier.SERCO, "from", "to", Money.Factory.valueOf(200.35))

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
    verify(priceRepository).save(price)
    verify(price).priceInPence = 20035
  }
}
