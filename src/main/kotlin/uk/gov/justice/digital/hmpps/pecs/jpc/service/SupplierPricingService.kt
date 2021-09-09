package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

@Service
@Transactional
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class SupplierPricingService(
  private val locationRepository: LocationRepository,
  private val priceRepository: PriceRepository,
  private val annualPriceAdjuster: AnnualPriceAdjuster,
  private val auditService: AuditService,
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

  fun getMaybeSiteNamesAndPrice(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int
  ): Triple<String, String, Money>? {
    return getFromAndToLocationBy(fromAgencyId, toAgencyId).let { (from, to) ->
      priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
        supplier,
        from,
        to,
        effectiveYear
      )?.let { Triple(from.siteName, to.siteName, Money(it.priceInPence)) }
    }
  }

  fun addPriceForSupplier(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    price: Money,
    effectiveYear: Int
  ) {
    failIfPriceAdjustmentInProgressFor(supplier)

    val (fromLocation, toLocation) = getFromAndToLocationBy(fromAgencyId, toAgencyId)

    priceRepository.save(
      Price(
        supplier = supplier,
        fromLocation = fromLocation,
        toLocation = toLocation,
        priceInPence = price.pence,
        effectiveYear = effectiveYear
      )
    ).let { auditService.create(AuditableEvent.addPrice(it, SecurityContextHolder.getContext().authentication)) }
  }

  fun updatePriceForSupplier(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    agreedNewPrice: Money,
    effectiveYear: Int
  ) {
    failIfPriceAdjustmentInProgressFor(supplier)

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

      priceRepository.save(existingPrice.apply { this.priceInPence = agreedNewPrice.pence })
        .let { auditService.create(AuditableEvent.updatePrice(it, oldPrice)) }
    }
  }

  private fun failIfPriceAdjustmentInProgressFor(supplier: Supplier) {
    if (annualPriceAdjuster.isInProgressFor(supplier)) throw RuntimeException("Price adjustment in currently progress for $supplier")
  }

  private fun getFromAndToLocationBy(from: String, to: String): Pair<Location, Location> =
    Pair(
      getLocationBy(from) ?: throw RuntimeException("From NOMIS agency id '$from' not found."),
      getLocationBy(to) ?: throw RuntimeException("To NOMIS agency id '$to' not found.")
    )

  private fun getLocationBy(agencyId: String): Location? = locationRepository.findByNomisAgencyId(sanitised(agencyId))

  fun priceHistoryForJourney(supplier: Supplier, fromAgencyId: String, toAgencyId: String): Set<AuditEvent> {
    return auditService.auditEventsByTypeAndMetaKey(
      AuditEventType.JOURNEY_PRICE,
      PriceMetadata.key(supplier, fromAgencyId, toAgencyId)
    )
      .associateWith { PriceMetadata.map(it) }
      .keys
  }

  private fun sanitised(value: String) = value.trim().uppercase()
}
