package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@Service
@Transactional
class SupplierPricingService(val locationRepository: LocationRepository, val priceRepository: PriceRepository) {

  fun getSiteNamesForPricing(supplier: Supplier, fromAgencyId: String, toAgencyId: String): Pair<String, String> {
    val fromLocation = locationRepository.findByNomisAgencyId(fromAgencyId.trim().toUpperCase())
            ?: throw RuntimeException("From NOMIS agency id '$fromAgencyId'not found.")

    val toLocation = locationRepository.findByNomisAgencyId(toAgencyId.trim().toUpperCase())
            ?: throw RuntimeException("To NOMIS agency id '$toAgencyId'not found.")

    priceRepository.findBySupplierAndFromLocationAndToLocation(supplier, fromLocation, toLocation)?.let { throw RuntimeException("Supplier $supplier price already exists from ${fromLocation.siteName} to ${toLocation.siteName}") }

    return Pair(fromLocation.siteName, toLocation.siteName)
  }

  fun addPriceForSupplier(supplier: Supplier, fromAgencyId: String, toAgencyId: String, price: Double) {
    val fromLocation = locationRepository.findByNomisAgencyId(fromAgencyId.trim().toUpperCase())
            ?: throw RuntimeException("From NOMIS agency id '$fromAgencyId'not found.")

    val toLocation = locationRepository.findByNomisAgencyId(toAgencyId.trim().toUpperCase())
            ?: throw RuntimeException("To NOMIS agency id '$toAgencyId'not found.")

    priceRepository.save(Price(supplier = supplier, fromLocation = fromLocation, toLocation = toLocation, priceInPence = price.toInt() * 100))
  }
}
