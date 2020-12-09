package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@Service
@Transactional
class SupplierPricingService(val locationRepository: LocationRepository, val priceRepository: PriceRepository) {

  fun getSiteNamesForPricing(supplier: Supplier, fromAgencyId: String, toAgencyId: String): Pair<String, String> {
    val locations = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.findBySupplierAndFromLocationAndToLocation(supplier, locations.first, locations.second)?.let {
      throw RuntimeException("Supplier $supplier price already exists from ${locations.first.siteName} to ${locations.second.siteName}")
    }

    return Pair(locations.first.siteName, locations.second.siteName)
  }

  fun getExistingSiteNamesAndPrice(supplier: Supplier, fromAgencyId: String, toAgencyId: String): Triple<String, String, Money> {
    val locations = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val price = priceRepository.findBySupplierAndFromLocationAndToLocation(supplier, locations.first, locations.second)
            ?: throw RuntimeException("No matching price found for $supplier")

    return Triple(locations.first.siteName, locations.second.siteName, Money(price.priceInPence))
  }

  fun addPriceForSupplier(supplier: Supplier, fromAgencyId: String, toAgencyId: String, price: Money) {
    val locations = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.save(Price(supplier = supplier, fromLocation = locations.first, toLocation = locations.second, priceInPence = price.pence, effectiveYear = 2020))
  }

  fun updatePriceForSupplier(supplier: Supplier, fromAgencyId: String, toAgencyId: String, agreedNewPrice: Money) {
    val locations = getFromAndToLocationBy(fromAgencyId, toAgencyId)
    val existingPrice = priceRepository.findBySupplierAndFromLocationAndToLocation(supplier, locations.first, locations.second)
            ?: throw RuntimeException("No matching price found for $supplier")

    priceRepository.save(existingPrice.apply { this.priceInPence = agreedNewPrice.pence })
  }

  private fun getFromAndToLocationBy(from: String, to: String): Pair<Location, Location> {
    val fromLocation = getLocationBy(from) ?: throw RuntimeException("From NOMIS agency id '$from' not found.")
    val toLocation = getLocationBy(to) ?: throw RuntimeException("To NOMIS agency id '$to' not found.")

    return Pair(fromLocation, toLocation)
  }

  private fun getLocationBy(agencyId: String): Location? = locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
}
