package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource

/**
 * Domain level service to perform the actual uplift for a supplier.
 */
@Component
class PriceUplifter(
  private val priceRepository: PriceRepository,
  private val priceUpliftRepository: SupplierPriceUpliftRepository,
  private val timeSource: TimeSource
) {

  /**
   * Any exception thrown calling this function will passed onto the onFailure function.
   */
  internal fun uplift(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double,
    onFailure: (e: Throwable) -> Unit,
    onSuccess: (upliftedPriceCount: Int) -> Unit
  ) {
    Result.runCatching {
      inProgress(supplier, effectiveYear, multiplier)
      applyPriceChanges(supplier, effectiveYear, multiplier)
    }.onFailure {
      done(supplier)
      onFailure(it)
    }.onSuccess {
      done(supplier)
      onSuccess(it)
    }
  }

  private fun inProgress(supplier: Supplier, effectiveYear: Int, multiplier: Double) {
    priceUpliftRepository.saveAndFlush(
      SupplierPriceUplift(
        supplier = supplier,
        effectiveYear = effectiveYear,
        multiplier = multiplier,
        addedAt = timeSource.dateTime()
      )
    )
  }

  private fun applyPriceChanges(supplier: Supplier, effectiveYear: Int, multiplier: Double): Int {
    // TODO to be implemented
    return -1
  }

  private fun done(supplier: Supplier) {
    with(priceUpliftRepository) {
      deleteBySupplier(supplier)
      flush()
    }
  }
}
