package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class SupplierPricingServiceTest {

  private val locationRepository: LocationRepository = mock()
  private val fromLocation: Location = mock { on { siteName } doReturn "from site" }
  private val toLocation: Location = mock { on { siteName } doReturn "to site" }
  private val priceRepository: PriceRepository = mock()
  private val price: Price = mock { on { priceInPence } doReturn 1000 }
  private val service: SupplierPricingService = SupplierPricingService(locationRepository, priceRepository)

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

    service.addPriceForSupplier(Supplier.SERCO, "from", "to", 100.0)

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).save(any())
  }

  @Test
  internal fun `existing site names and price returned`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)).thenReturn(price)

    val result = service.getExistingSiteNamesAndPrice(Supplier.SERCO, "from", "to")

    assertThat(result).isEqualTo(Triple("from site", "to site", 10.0))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
  }

  @Test
  internal fun `update existing price for supplier`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(priceRepository.findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)).thenReturn(price)

    service.updatePriceForSupplier(Supplier.SERCO, "from", "to", 200.0)

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
    verify(priceRepository).save(any())
  }
}
