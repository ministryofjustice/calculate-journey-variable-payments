package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.util.UUID
import kotlin.streams.asSequence

/**
 * Domain level service to perform the annual price adjustments for a supplier.
 */
private val logger = loggerFor<AnnualPriceAdjuster>()

@Component
@Transactional
class AnnualPriceAdjuster(
  private val priceRepository: PriceRepository,
  private val priceAdjustmentRepository: PriceAdjustmentRepository,
  private val auditService: AuditService,
  private val timeSource: TimeSource
) {

  internal fun isInProgressFor(supplier: Supplier) = priceAdjustmentRepository.existsPriceAdjustmentBySupplier(supplier)

  /**
   * Inflationary adjustments for the supplied effective year are calculated based on prices for the previous year with
   * the supplied multiplier.
   */
  internal fun inflationary(
    id: UUID,
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: AdjustmentMultiplier,
  ) = adjust(id, supplier, effectiveYear, multiplier, true)

  /**
   * Volumetric adjustments for the supplied effective year are calculated based on prices for the current year with
   * the supplied multiplier.
   */
  internal fun volumetric(
    id: UUID,
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: AdjustmentMultiplier,
  ) = adjust(id, supplier, effectiveYear, multiplier, false)

  private fun adjust(
    id: UUID,
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: AdjustmentMultiplier,
    basedOnPreviousYearsPrices: Boolean
  ): Int {
    priceAdjustmentRepository.failIfLockNotPresent(id, supplier)

    return applyPriceAdjustmentsForSupplierAndEffectiveYear(supplier, effectiveYear, multiplier, basedOnPreviousYearsPrices)
  }

  fun PriceAdjustmentRepository.failIfLockNotPresent(id: UUID, supplier: Supplier) {
    if (!priceAdjustmentRepository.existsById(id)) throw RuntimeException("Unable to upflift lock is not present for $supplier.")
  }

  /**
   * There can only ever be one supplier price adjustment in progress. This will fail (as expected) if one already exists!
   *
   * Returns the ID of the lock (if successfully created).
   */
  internal fun attemptLockForPriceAdjustment(supplier: Supplier, multiplier: AdjustmentMultiplier, effectiveYear: Int) =
    priceAdjustmentRepository.saveAndFlush(
      PriceAdjustment(
        supplier = supplier,
        addedAt = timeSource.dateTime(),
        multiplier = multiplier.value,
        effectiveYear = effectiveYear
      )
    ).id

  private fun applyPriceAdjustmentsForSupplierAndEffectiveYear(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: AdjustmentMultiplier,
    basedOnPreviousYearsPrices: Boolean
  ): Int {
    var adjustments = 0

    return priceRepository
      .possiblePricesForAdjustment(supplier, if (basedOnPreviousYearsPrices) effectiveYear - 1 else effectiveYear)
      .map { maybePriceAdjustment(it, effectiveYear, multiplier) }
      .filterNotNull()
      .map {
        priceRepository.save(it)
        showProgressForEveryOneHundredPrice(adjustments++)
      }
      .count()
  }

  private fun showProgressForEveryOneHundredPrice(adjustments: Int) {
    if (adjustments % 100 == 0) logger.info("$adjustments prices adjusted...")
  }

  private fun PriceRepository.possiblePricesForAdjustment(supplier: Supplier, effectiveYear: Int) =
    this.findBySupplierAndEffectiveYear(supplier, effectiveYear).asSequence()

  private fun maybePriceAdjustment(previousYearPrice: Price, effectiveYear: Int, multiplier: AdjustmentMultiplier): Price? {
    val existingAdjustedPrice = maybeExistingAdjustedPrice(previousYearPrice, effectiveYear)

    if (existingAdjustedPrice != null) {
      val calculatedAdjustmentAmount = multiplier * previousYearPrice.price()

      return existingAdjustedPrice
        .takeExistingPriceUnlessTheSameAs(calculatedAdjustmentAmount)
        ?.adjustPriceTo(calculatedAdjustmentAmount)
        ?.auditedAdjustment(previousYearPrice, multiplier)
    }

    return newPriceAdjustmentFor(previousYearPrice, effectiveYear, multiplier)
      .auditedAdjustment(previousYearPrice, multiplier)
  }

  private fun Price.takeExistingPriceUnlessTheSameAs(amount: Money) = this.takeUnless { it.price() == amount }

  private fun Price.adjustPriceTo(money: Money) = this.apply { priceInPence = money.pence }

  private fun Price.auditedAdjustment(previousPrice: Price, multiplier: AdjustmentMultiplier) =
    this.also { auditService.create(AuditableEvent.adjustPrice(this, previousPrice.price(), multiplier)) }

  private fun newPriceAdjustmentFor(price: Price, effectiveYear: Int, multiplier: AdjustmentMultiplier) =
    price.adjusted(
      amount = multiplier * price.price(),
      effectiveYear = effectiveYear,
      addedAt = timeSource.dateTime()
    )

  private fun maybeExistingAdjustedPrice(price: Price, effectiveYear: Int) =
    priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      price.supplier,
      price.fromLocation,
      price.toLocation,
      effectiveYear
    )

  internal fun releaseLockForPriceAdjustment(id: UUID) {
    with(priceAdjustmentRepository) {
      deleteById(id)
      flush()
    }
  }
}
