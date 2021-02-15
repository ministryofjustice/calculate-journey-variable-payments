package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime

internal class SupplierPricingServiceTest {

  private val auditService: AuditService = mock()
  private val priceInPence: Int = 10024
  private val locationRepository: LocationRepository = mock()
  private val fromLocation: Location = mock { on { siteName } doReturn "from site" }
  private val toLocation: Location = mock { on { siteName } doReturn "to site" }
  private val priceRepository: PriceRepository = mock()
  private val price: Price = mock {
    on { priceInPence } doReturn priceInPence
    on { price() } doReturn Money.Factory.valueOf(priceInPence / 100.0)
  }
  private val service: SupplierPricingService =
    SupplierPricingService(
      locationRepository,
      priceRepository,
      { LocalDateTime.of(2021, 1, 1, 12, 0) },
      auditService
    )
  private val priceCaptor = argumentCaptor<Price>()
  private val authentication: Authentication = mock { on { name } doReturn " mOcK NAME      " }
  private val securityContext: SecurityContext = mock { on { authentication } doReturn authentication }

  private val effectiveDate = LocalDate.now()

  @BeforeEach
  private fun initMocks() {
    SecurityContextHolder.setContext(securityContext)
  }

  @AfterEach
  private fun deInitMocks() {
    SecurityContextHolder.clearContext()
  }

  @Test
  internal fun `site names returned for new pricing`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocation(
        Supplier.SERCO,
        fromLocation,
        toLocation
      )
    ).thenReturn(null)

    val result = service.getSiteNamesForPricing(Supplier.SERCO, "from", "to", effectiveYearForDate(effectiveDate))

    assertThat(result).isEqualTo(Pair("from site", "to site"))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYearForDate(effectiveDate)
    )
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
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocation(
        Supplier.SERCO,
        fromLocation,
        toLocation
      )
    ).thenReturn(price)

    val result = service.getExistingSiteNamesAndPrice(Supplier.SERCO, "from", "to", effectiveYearForDate(effectiveDate))

    assertThat(result).isEqualTo(Triple("from site", "to site", Money.valueOf(100.24)))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocation(Supplier.SERCO, fromLocation, toLocation)
  }

  @Test
  internal fun `update existing price for supplier`() {
    whenever(locationRepository.findByNomisAgencyId("FROM")).thenReturn(fromLocation)
    whenever(locationRepository.findByNomisAgencyId("TO")).thenReturn(toLocation)
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYearForDate(effectiveDate)
      )
    ).thenReturn(price)

    service.updatePriceForSupplier(Supplier.SERCO, "from", "to", Money.Factory.valueOf(200.35))

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYearForDate(effectiveDate)
    )
    verify(priceRepository).save(price)
    verify(price).priceInPence = 20035
  }
}
