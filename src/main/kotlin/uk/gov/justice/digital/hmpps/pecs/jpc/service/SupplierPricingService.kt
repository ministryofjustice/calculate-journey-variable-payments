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
import java.time.Month

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

  fun maybePrice(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int
  ): PriceDto? = existingPriceOrNull(supplier, fromAgencyId, toAgencyId, effectiveYear)?.let { price ->
    PriceDto(
      price.fromLocation.siteName,
      price.toLocation.siteName,
      price.price()
    ).apply { price.exceptions().forEach { exceptions[it.month] = it.price() } }
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

    val existingPrice = existingPriceOrNull(supplier, fromAgencyId, toAgencyId, effectiveYear)
      ?: throw RuntimeException("No matching price found for $supplier")

    if (existingPrice.price() != agreedNewPrice) {
      val oldPrice = existingPrice.price().copy()

      priceRepository.save(existingPrice.apply { this.priceInPence = agreedNewPrice.pence })
        .let { auditService.create(AuditableEvent.updatePrice(it, oldPrice)) }
    }
  }

  fun addPriceException(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int,
    month: Month,
    amount: Money
  ) {
    val existingPrice = existingPriceOrNull(supplier, fromAgencyId, toAgencyId, effectiveYear)
      ?.apply { addException(month, amount) } ?: throw RuntimeException("No matching price found for $supplier")

    priceRepository.save(existingPrice)
    auditService.create(AuditableEvent.addPriceException(existingPrice, month, amount))
  }

  private fun existingPriceOrNull(
    supplier: Supplier,
    fromAgencyId: String,
    toAgencyId: String,
    effectiveYear: Int,
  ) = getFromAndToLocationBy(fromAgencyId, toAgencyId).let { (from, to) ->
    priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      supplier,
      from,
      to,
      effectiveYear
    )
  }

  private fun failIfPriceAdjustmentInProgressFor(supplier: Supplier) {
    if (annualPriceAdjuster.isInProgressFor(supplier)) throw RuntimeException("Price adjustment in currently progress for $supplier")
  }

  private fun getFromAndToLocationBy(from: String, to: String): Pair<Location, Location> =
    Pair(
      getLocationBy(from) ?: throw RuntimeException("From NOMIS agency id '$from' not found."),
      getLocationBy(to) ?: throw RuntimeException("To NOMIS agency id '$to' not found.")
    )

  private fun getLocationBy(agencyId: String): Location? = locationRepository.findByNomisAgencyId(agencyId.sanitised())

  fun priceHistoryForJourney(supplier: Supplier, fromAgencyId: String, toAgencyId: String): Set<AuditEvent> {
    return auditService.auditEventsByTypeAndMetaKey(
      AuditEventType.JOURNEY_PRICE,
      PriceMetadata.key(supplier, fromAgencyId, toAgencyId)
    )
      .associateWith { PriceMetadata.map(it) }
      .keys
  }

  data class PriceDto(val fromAgency: String, val toAgency: String, val amount: Money) {
    val exceptions: MutableMap<Int, Money> = mutableMapOf()
  }

  private fun String.sanitised() = this.trim().uppercase()
}
