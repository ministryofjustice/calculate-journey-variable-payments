package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate

@Service
@Transactional
class SupplierPricingService(
  val locationRepository: LocationRepository,
  val priceRepository: PriceRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService
) {
  fun getSiteNamesForPricing(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int
  ): Pair<String, String> {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(supplier, fromLocation, toLocation, effectiveYear)
      ?.let {
        throw RuntimeException("Supplier $supplier price already exists from ${fromLocation.siteName} to ${toLocation.siteName}")
      }

    return Pair(fromLocation.siteName, toLocation.siteName)
  }

  fun getExistingSiteNamesAndPrice(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int
  ): Triple<String, String, Money> {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val price = priceRepository.findBySupplierAndFromLocationAndToLocation(supplier, fromLocation, toLocation)
      ?: throw RuntimeException("No matching price found for $supplier")

    return Triple(fromLocation.siteName, toLocation.siteName, Money(price.priceInPence))
  }

  fun addPriceForSupplier(supplier: Supplier, fromAgencyId: String, toAgencyId: String, price: Money) {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val effectiveYear = effectiveYearForDate(timeSource.date())

    priceRepository.save(
      Price(
        supplier = supplier,
        fromLocation = fromLocation,
        toLocation = toLocation,
        priceInPence = price.pence,
        effectiveYear = effectiveYear
      )
    )

    auditService.create(
      AuditableEvent.createJourneyPriceEvent(
        supplier,
        fromAgencyId,
        toAgencyId,
        effectiveYear,
        price,
        timeSource = timeSource
      )
    )
  }

  fun updatePriceForSupplier(supplier: Supplier, fromAgencyId: String, toAgencyId: String, agreedNewPrice: Money) {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val effectiveYear = effectiveYearForDate(timeSource.date())
    val existingPrice = priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      supplier,
      fromLocation,
      toLocation,
      effectiveYear
    )
      ?: throw RuntimeException("No matching price found for $supplier")

    val oldPrice = Money.valueOf(existingPrice.price().pounds())

    priceRepository.save(existingPrice.apply { this.priceInPence = agreedNewPrice.pence })
    auditService.create(
      AuditableEvent.createJourneyPriceEvent(
        supplier,
        fromAgencyId,
        toAgencyId,
        effectiveYear,
        oldPrice,
        agreedNewPrice,
        timeSource
      )
    )
  }

  private fun getFromAndToLocationBy(from: String, to: String): Pair<Location, Location> =
    Pair(
      getLocationBy(from) ?: throw RuntimeException("From NOMIS agency id '$from' not found."),
      getLocationBy(to) ?: throw RuntimeException("To NOMIS agency id '$to' not found.")
    )

  private fun getLocationBy(agencyId: String): Location? =
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
}
