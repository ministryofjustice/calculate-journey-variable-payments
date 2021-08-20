package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.util.UUID
import kotlin.streams.asSequence

/**
 * Domain level service to perform the annual price adjustments for a supplier.
 */
@Component
@Transactional
class AnnualPriceAdjuster(
  private val priceRepository: PriceRepository,
  private val priceAdjustmentRepository: PriceAdjustmentRepository,
  private val auditService: AuditService,
  private val timeSource: TimeSource
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  internal fun isInProgressFor(supplier: Supplier) = priceAdjustmentRepository.existsPriceAdjustmentBySupplier(supplier)

  /**
   * Price adjustments for the supplied effective year are calculated based on prices for the previous year with the supplied multiplier.
   */
  internal fun adjust(
    id: UUID,
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double,
  ): Int {
    priceAdjustmentRepository.failIfLockNotPresent(id, supplier)

    return applyPriceAdjustmentsForSupplierAndEffectiveYear(supplier, effectiveYear, multiplier)
  }

  fun PriceAdjustmentRepository.failIfLockNotPresent(id: UUID, supplier: Supplier) {
    if (!priceAdjustmentRepository.existsById(id)) throw RuntimeException("Unable to upflift lock is not present for $supplier.")
  }

  /**
   * There can only every be one supplier price adjustment in progress. This will fail (as expected) if one already exists!
   *
   * Returns the ID of the lock (if successfully created).
   */
  internal fun attemptLockForPriceAdjustment(supplier: Supplier, multiplier: Double, effectiveYear: Int) =
    priceAdjustmentRepository.saveAndFlush(
      PriceAdjustment(
        supplier = supplier,
        addedAt = timeSource.dateTime(),
        multiplier = multiplier,
        effectiveYear = effectiveYear
      )
    ).id

  private fun applyPriceAdjustmentsForSupplierAndEffectiveYear(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double
  ): Int {
    var adjustments = 0

    return priceRepository
      .possiblePricesForAdjustment(supplier, effectiveYear)
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
    this.findBySupplierAndEffectiveYear(supplier, effectiveYear - 1).asSequence()

  private fun maybePriceAdjustment(previousYearPrice: Price, effectiveYear: Int, multiplier: Double): Price? {
    val existingAdjustedPrice = maybeExistingAdjustedPrice(previousYearPrice, effectiveYear)

    if (existingAdjustedPrice != null) {
      return existingAdjustedPrice
        .takeUnless { priceIsTheSame(it, previousYearPrice.price().times(multiplier)) }
        ?.apply { priceInPence = previousYearPrice.price().times(multiplier).pence }
        ?.also {
          auditService.create(AuditableEvent.adjustPrice(it, previousYearPrice.price(), multiplier))
        }
    }

    return newPriceAdjustmentFor(previousYearPrice, effectiveYear, multiplier).also {
      auditService.create(AuditableEvent.adjustPrice(it, previousYearPrice.price(), multiplier))
    }
  }

  private fun priceIsTheSame(price: Price, amount: Money) = price.price() == amount

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

  internal fun releaseLockForPriceAdjustment(id: UUID) {
    with(priceAdjustmentRepository) {
      deleteById(id)
      flush()
    }
  }
}
