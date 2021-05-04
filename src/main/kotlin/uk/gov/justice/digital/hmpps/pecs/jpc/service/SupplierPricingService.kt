package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@Service
@Transactional
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class SupplierPricingService(
  val locationRepository: LocationRepository,
  val priceRepository: PriceRepository,
  private val auditService: AuditService
) {
  fun getSiteNamesForPricing(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int
  ): Pair<String, String> {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      supplier,
      fromLocation,
      toLocation,
      effectiveYear
    )
      ?.let {
        throw RuntimeException("Supplier $supplier price already exists from ${fromLocation.siteName} to ${toLocation.siteName} for $effectiveYear")
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

  fun addPriceForSupplier(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    price: Money,
    effectiveYear: Int
  ) {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.save(
      Price(
        supplier = supplier,
        fromLocation = fromLocation,
        toLocation = toLocation,
        priceInPence = price.pence,
        effectiveYear = effectiveYear
      )
    ).let { auditService.create(AuditableEvent.addPrice(it)) }
  }

  fun updatePriceForSupplier(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    agreedNewPrice: Money,
    effectiveYear: Int
  ) {
    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val existingPrice = priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      supplier,
      fromLocation,
      toLocation,
      effectiveYear
    )
      ?: throw RuntimeException("No matching price found for $supplier")

    if (existingPrice.price() != agreedNewPrice) {
      val oldPrice = existingPrice.price().copy()

      priceRepository.save(existingPrice.apply { this.priceInPence = agreedNewPrice.pence }).let { auditService.create(AuditableEvent.updatePrice(it, oldPrice)) }
    }
  }

  private fun getFromAndToLocationBy(from: String, to: String): Pair<Location, Location> =
    Pair(
      getLocationBy(from) ?: throw RuntimeException("From NOMIS agency id '$from' not found."),
      getLocationBy(to) ?: throw RuntimeException("To NOMIS agency id '$to' not found.")
    )

  private fun getLocationBy(agencyId: String): Location? =
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
}
