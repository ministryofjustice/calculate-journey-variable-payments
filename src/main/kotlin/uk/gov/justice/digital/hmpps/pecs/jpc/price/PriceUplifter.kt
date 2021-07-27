package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource

/**
 * Domain level service to perform the actual price adjustments for a supplier.
 *
 * Prices for the supplied effective year are calculated based on prices for the previous year with the supplied multiplier.
 */
@Component
class PriceUplifter(
  private val priceRepository: PriceRepository,
  private val priceUpliftRepository: SupplierPriceUpliftRepository,
  private val timeSource: TimeSource
) {

  /**
   * Any exception thrown calling this function will passed onto the onFailure lambda function.
   */
  internal fun uplift(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double,
    onFailure: (e: Throwable) -> Unit,
    onSuccess: (upliftedPriceCount: Int) -> Unit
  ) {
    Result.runCatching {
      upliftInProgress(supplier, effectiveYear, multiplier)
      upliftPrices(supplier, effectiveYear, multiplier)
    }.onFailure {
      upliftDone(supplier)
      onFailure(it)
    }.onSuccess {
      upliftDone(supplier)
      onSuccess(it)
    }
  }

  private fun upliftInProgress(supplier: Supplier, effectiveYear: Int, multiplier: Double) {
    priceUpliftRepository.saveAndFlush(
      SupplierPriceUplift(
        supplier = supplier,
        effectiveYear = effectiveYear,
        multiplier = multiplier,
        addedAt = timeSource.dateTime()
      )
    )
  }

  private fun upliftPrices(supplier: Supplier, effectiveYear: Int, multiplier: Double) =
    priceRepository
      .previousYearPrices(supplier, effectiveYear)
      .map {
        priceRepository.save(upliftedPriceAdjustment(it, effectiveYear, multiplier))
      }.count().toInt()

  private fun PriceRepository.previousYearPrices(supplier: Supplier, effectiveYear: Int) =
    this.findBySupplierAndEffectiveYear(supplier, effectiveYear - 1)

  private fun upliftedPriceAdjustment(previousYearPrice: Price, effectiveYear: Int, multiplier: Double): Price {
    return priceRepository.findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
      previousYearPrice.supplier,
      previousYearPrice.fromLocation,
      previousYearPrice.toLocation,
      effectiveYear
    )?.apply { this.priceInPence = previousYearPrice.price().times(multiplier).pence } ?: previousYearPrice.adjusted(
      amount = previousYearPrice.price().times(multiplier),
      effectiveYear = effectiveYear,
      addedAt = timeSource.dateTime()
    )
  }

  private fun upliftDone(supplier: Supplier) {
    with(priceUpliftRepository) {
      deleteBySupplier(supplier)
      flush()
    }
  }
}
