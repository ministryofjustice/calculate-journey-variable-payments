package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType.JOURNEY_PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.FakeAuthentication
import java.time.LocalDateTime
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import java.time.Month.JULY
import java.time.Month.SEPTEMBER

@ExtendWith(FakeAuthentication::class)
internal class SupplierPricingServiceTest {

  private val annualPriceAdjuster: AnnualPriceAdjuster = mock()
  private val auditService: AuditService = mock()
  private val effectiveYearDate = LocalDateTime.of(2022, DECEMBER, 25, 12, 0)
  private val effectiveYear = effectiveYearForDate(effectiveYearDate.toLocalDate())
  private val fromLocation: Location =
    Location(locationType = LocationType.PR, nomisAgencyId = "PRISON", siteName = "from site")
  private val toLocation: Location =
    Location(locationType = LocationType.MC, nomisAgencyId = "COURT", siteName = "to site")
  private val locationRepository: LocationRepository = mock {
    on { findByNomisAgencyId("FROM") } doReturn fromLocation
    on { findByNomisAgencyId("TO") } doReturn toLocation
  }
  private val priceRepository: PriceRepository = mock()
  private val sercoPrice: Price = Price(
    supplier = Supplier.SERCO,
    priceInPence = 10024,
    fromLocation = fromLocation,
    toLocation = toLocation,
    effectiveYear = effectiveYear,
  )
  private val priceCaptor = argumentCaptor<Price>()
  private val eventCaptor = argumentCaptor<AuditableEvent>()
  private val actualEffectYear: EffectiveYear = EffectiveYear({ effectiveYearDate })

  private val service: SupplierPricingService =
    SupplierPricingService(locationRepository, priceRepository, annualPriceAdjuster, auditService, actualEffectYear)

  @Test
  internal fun `site names returned for new pricing`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocation(
        Supplier.SERCO,
        fromLocation,
        toLocation,
      ),
    ).thenReturn(null)

    val siteNames = service.getSiteNamesForPricing(Supplier.SERCO, "from", "to", effectiveYear)

    assertThat(siteNames).isEqualTo(Pair("from site", "to site"))
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYear,
    )
  }

  @Test
  internal fun `add new price for supplier`() {
    whenever(priceRepository.save(any())).thenReturn(sercoPrice)

    service.addPriceForSupplier(
      Supplier.SERCO,
      "from",
      "to",
      Money.valueOf("100.24"),
      effectiveYear,
    )

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).save(priceCaptor.capture())

    assertThat(priceCaptor.firstValue.priceInPence).isEqualTo(10024)
    verify(auditService).create(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.type).isEqualTo(JOURNEY_PRICE)
  }

  @Test
  internal fun `add new price fails for supplier if price adjustment in progress`() {
    whenever(annualPriceAdjuster.isInProgressFor(Supplier.SERCO)).thenReturn(true)

    assertThatThrownBy {
      service.addPriceForSupplier(
        Supplier.SERCO,
        "from",
        "to",
        Money.valueOf("100.24"),
        effectiveYear,
      )
    }.isInstanceOf(RuntimeException::class.java).hasMessage("Price adjustment in currently progress for SERCO")

    verify(priceRepository, never()).save(any())
  }

  @Test
  internal fun `add new price fails for supplier if outside of price change window`() {
    assertThatThrownBy {
      service.addPriceForSupplier(
        Supplier.SERCO,
        "from",
        "to",
        Money.valueOf("100.24"),
        effectiveYear - 2,
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price changes can no longer be made, change is outside of price change window.")

    verify(priceRepository, never()).save(any())
  }

  @Test
  internal fun `existing price without exceptions returned`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(sercoPrice)

    val existingPrice = service.maybePrice(Supplier.SERCO, "from", "to", effectiveYear)

    assertThat(existingPrice).isEqualTo(
      SupplierPricingService.PriceDto(
        "from site",
        "to site",
        Money.valueOf("100.24"),
      ),
    )
    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYear,
    )
  }

  @Test
  internal fun `existing price with exceptions returned`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(sercoPrice.addException(JANUARY, Money(200)))

    val existingPrice = service.maybePrice(Supplier.SERCO, "from", "to", effectiveYear)

    assertThat(existingPrice).isEqualTo(
      SupplierPricingService.PriceDto(
        "from site",
        "to site",
        Money.valueOf("100.24"),
      ).apply { exceptions[1] = Money(200) },
    )

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYear,
    )
  }

  @Test
  internal fun `attempt to update existing price to same price has no effect`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(sercoPrice)
    whenever(priceRepository.save(any())).thenReturn(sercoPrice)

    service.updatePriceForSupplier(
      Supplier.SERCO,
      "from",
      "to",
      sercoPrice.price(),
      effectiveYear,
    )

    verify(priceRepository, never()).save(any())
    verify(auditService, never()).create(any())
  }

  @Test
  internal fun `update existing price for supplier`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(sercoPrice)
    whenever(priceRepository.save(any())).thenReturn(sercoPrice)

    service.updatePriceForSupplier(
      Supplier.SERCO,
      "from",
      "to",
      Money.valueOf("200.35"),
      effectiveYear,
    )

    verify(locationRepository).findByNomisAgencyId("FROM")
    verify(locationRepository).findByNomisAgencyId("TO")
    verify(priceRepository).findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      Supplier.SERCO,
      fromLocation,
      toLocation,
      effectiveYear,
    )
    verify(priceRepository).save(sercoPrice)
    assertThat(sercoPrice.priceInPence).isEqualTo(20035)
    verify(auditService).create(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.type).isEqualTo(JOURNEY_PRICE)
  }

  @Test
  internal fun `update existing price for supplier fails if price adjustment in progress`() {
    whenever(annualPriceAdjuster.isInProgressFor(Supplier.GEOAMEY)).thenReturn(true)

    assertThatThrownBy {
      service.updatePriceForSupplier(
        Supplier.GEOAMEY,
        "from",
        "to",
        Money.valueOf("200.35"),
        effectiveYear,
      )
    }.isInstanceOf(RuntimeException::class.java).hasMessage("Price adjustment in currently progress for GEOAMEY")

    verify(priceRepository, never()).save(any())
  }

  @Test
  internal fun `update existing price fails for supplier if outside of price change window`() {
    assertThatThrownBy {
      service.updatePriceForSupplier(
        Supplier.GEOAMEY,
        "from",
        "to",
        Money.valueOf("200.35"),
        effectiveYear - 2,
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price changes can no longer be made, change is outside of price change window.")

    verify(priceRepository, never()).save(any())
  }

  @Test
  internal fun `finds single price history entry for Serco`() {
    val sercoOriginalPrice = PriceMetadata(Supplier.SERCO, "FROM_AGENCY_ID", "TO_AGENCY_ID", 2021, Money(1000).pounds())
    val sercoOriginalPriceEvent =
      AuditEvent(JOURNEY_PRICE, LocalDateTime.now(), "Jane", sercoOriginalPrice)

    whenever(
      auditService.auditEventsByTypeAndMetaKey(
        JOURNEY_PRICE,
        "SERCO-FROM_AGENCY_ID-TO_AGENCY_ID",
      ),
    ).thenReturn(listOf(sercoOriginalPriceEvent))

    val sercoPriceHistory = service.priceHistoryForJourney(Supplier.SERCO, "from_agency_id", "to_agency_id")

    assertThat(sercoPriceHistory).containsExactly(sercoOriginalPriceEvent)
    verify(auditService).auditEventsByTypeAndMetaKey(JOURNEY_PRICE, "SERCO-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  internal fun `finds multiple price history entries for GEOAmey`() {
    val geoameyOriginalPrice =
      PriceMetadata(Supplier.GEOAMEY, "FROM_AGENCY_ID", "TO_AGENCY_ID", 2021, Money(1000).pounds())
    val geoameyOriginalPriceEvent =
      AuditEvent(JOURNEY_PRICE, LocalDateTime.now(), "Jane", geoameyOriginalPrice)

    val geoameyPriceChange = geoameyOriginalPrice.copy(newPrice = Money(2000).pounds(), oldPrice = Money(1000).pounds())
    val geoameyPriceChangeEvent =
      AuditEvent(JOURNEY_PRICE, LocalDateTime.now().plusDays(1), "Jane", geoameyPriceChange)

    whenever(
      auditService.auditEventsByTypeAndMetaKey(
        JOURNEY_PRICE,
        "GEOAMEY-FROM_AGENCY_ID-TO_AGENCY_ID",
      ),
    ).thenReturn(listOf(geoameyOriginalPriceEvent, geoameyPriceChangeEvent))

    val geoameyPriceHistory = service.priceHistoryForJourney(Supplier.GEOAMEY, "from_agency_id", "to_agency_id")

    assertThat(geoameyPriceHistory).containsExactly(geoameyOriginalPriceEvent, geoameyPriceChangeEvent)

    verify(auditService).auditEventsByTypeAndMetaKey(JOURNEY_PRICE, "GEOAMEY-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  internal fun `add price exception for existing price for Serco`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(sercoPrice)

    service.addPriceException(Supplier.SERCO, "FROM", "TO", effectiveYear, SEPTEMBER, Money.valueOf("20.00"))

    verify(priceRepository).save(sercoPrice.addException(SEPTEMBER, Money.valueOf("20.00")))
    verify(auditService).create(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.type).isEqualTo(JOURNEY_PRICE)
  }

  @Test
  internal fun `add price exception fails if outside of price change window `() {
    assertThatThrownBy {
      service.addPriceException(Supplier.SERCO, "FROM", "TO", effectiveYear - 2, SEPTEMBER, Money.valueOf("20.00"))
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price changes can no longer be made, change is outside of price change window.")
  }

  @Test
  internal fun `add price exception fails for Serco when price not found`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(null)

    assertThatThrownBy {
      service.addPriceException(Supplier.SERCO, "FROM", "TO", effectiveYear, SEPTEMBER, Money.valueOf("20.00"))
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("No matching price found for SERCO")
  }

  @Test
  internal fun `remove price exception`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(
      sercoPrice.addException(JULY, Money.valueOf("500.00")),
    )

    val priceWithExceptionRemoved =
      service.removePriceException(Supplier.SERCO, "FROM", "TO", sercoPrice.effectiveYear, JULY)

    assertThat(priceWithExceptionRemoved.exceptions).isEmpty()
    assertThat(sercoPrice.exceptionFor(JULY)).isNull()
    verify(priceRepository).save(sercoPrice)
    verify(auditService).create(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.type).isEqualTo(JOURNEY_PRICE)
  }

  @Test
  internal fun `remove price exception fails if outside of price change window`() {
    assertThatThrownBy {
      service.removePriceException(Supplier.SERCO, "FROM", "TO", effectiveYear - 2, JULY)
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price changes can no longer be made, change is outside of price change window.")
  }

  @Test
  internal fun `remove price exception fails for Serco when price not found`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(null)

    assertThatThrownBy {
      service.removePriceException(Supplier.SERCO, "FROM", "TO", effectiveYear, SEPTEMBER)
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("No matching price found for SERCO")
  }

  @Test
  internal fun `remove price exception fails when no matching exception`() {
    whenever(
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        Supplier.SERCO,
        fromLocation,
        toLocation,
        effectiveYear,
      ),
    ).thenReturn(
      sercoPrice.addException(JULY, Money.valueOf("500.00")),
    )

    assertThatThrownBy {
      service.removePriceException(Supplier.SERCO, "FROM", "TO", sercoPrice.effectiveYear, SEPTEMBER)
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("No matching price exception found in SEPTEMBER for SERCO")
  }
}
