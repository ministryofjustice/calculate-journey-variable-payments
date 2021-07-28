package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import kotlin.streams.asSequence

/**
 * Domain level service to perform the annual price adjustments for a supplier.
 */
@Component
class AnnualPriceAdjuster(
  private val priceRepository: PriceRepository,
  private val priceUpliftRepository: PriceAdjustmentRepository,
  private val timeSource: TimeSource
) {
  /**
   * Prices for the supplied effective year are calculated based on prices for the previous year with the supplied multiplier.
   *
   * Any exception thrown calling this function will be passed onto the onFailure lambda function.
   */
  internal fun uplift(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double,
    onFailure: (e: Throwable) -> Unit,
    onSuccess: (upliftedPriceCount: Int) -> Unit
  ) {
    Result.runCatching {
      attemptDatabaseLockForPriceAdjustment(supplier)
      applyPriceAdjustmentsForSupplierAndEffectiveYear(supplier, effectiveYear, multiplier)
    }.onFailure {
      releaseDatabaseLockForPriceAdjustment(supplier)
      onFailure(it)
    }.onSuccess {
      releaseDatabaseLockForPriceAdjustment(supplier)
      onSuccess(it)
    }
  }

  /**
   * There can only every be one supplier price adjustment in progress. This will fail (as expected) if one already exists!
   */
  private fun attemptDatabaseLockForPriceAdjustment(supplier: Supplier) {
    priceUpliftRepository.saveAndFlush(
      PriceAdjustment(
        supplier = supplier,
        addedAt = timeSource.dateTime()
      )
    )
  }

  private fun applyPriceAdjustmentsForSupplierAndEffectiveYear(supplier: Supplier, effectiveYear: Int, multiplier: Double) =
    priceRepository
      .possiblePricesForAdjustment(supplier, effectiveYear)
      .map { maybePriceAdjustment(it, effectiveYear, multiplier) }
      .filterNotNull()
      .map { priceRepository.save(it) }
      .count()

  private fun PriceRepository.possiblePricesForAdjustment(supplier: Supplier, effectiveYear: Int) =
    this.findBySupplierAndEffectiveYear(supplier, effectiveYear - 1).asSequence()

  private fun maybePriceAdjustment(previousYearPrice: Price, effectiveYear: Int, multiplier: Double): Price? {
    val existingAdjustedPrice = maybeExistingAdjustedPrice(previousYearPrice, effectiveYear)

    if (existingAdjustedPrice != null) {
      return existingAdjustedPrice
        .takeUnless { it.price() == previousYearPrice.price().times(multiplier) }
        ?.apply { priceInPence = previousYearPrice.price().times(multiplier).pence }
    }

    return newPriceAdjustmentFor(previousYearPrice, effectiveYear, multiplier)
  }

  private fun newPriceAdjustmentFor(price: Price, effectiveYear: Int, multiplier: Double) =
    price.adjusted(
      amount = price.price().times(multiplier),
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

  private fun releaseDatabaseLockForPriceAdjustment(supplier: Supplier) {
    with(priceUpliftRepository) {
      deleteBySupplier(supplier)
      flush()
    }
  }
}
