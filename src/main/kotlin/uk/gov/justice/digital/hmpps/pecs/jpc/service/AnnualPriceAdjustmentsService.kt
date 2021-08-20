package uk.gov.justice.digital.hmpps.pecs.jpc.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

/**
 * Service to handle annual price adjustments for suppliers.
 *
 * Generally speaking (but not always) annual adjustments take place at the start of a new effective year e.g. September.
 */
@Service
class AnnualPriceAdjustmentsService(
  private val annualPriceAdjuster: AnnualPriceAdjuster,
  private val monitoringService: MonitoringService,
  private val auditService: AuditService,
  private val actualEffectiveYear: EffectiveYear
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Price adjustments cannot be before the current effective year, if the supplied effective year is before it then an
   * exception will be thrown.
   *
   * An adjustment normally takes place at the start of the effective year is based around inflationary price rises.
   */
  fun adjust(supplier: Supplier, suppliedEffective: Int, multiplier: Double) {
    if (suppliedEffective < actualEffectiveYear.current()) {
      throw RuntimeException("Price adjustments cannot be before the current effective year ${actualEffectiveYear.current()}.")
    }

    logger.info("Starting price adjustment for $supplier for effective year $suppliedEffective using multiplier $multiplier.")

    doAdjustment(supplier, suppliedEffective, multiplier)
  }

  private fun doAdjustment(supplier: Supplier, effectiveYear: Int, multiplier: Double) = runBlocking {
    launch {
      Result.runCatching {
        val lockId = annualPriceAdjuster.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)

        annualPriceAdjuster.adjust(
          lockId,
          supplier,
          effectiveYear,
          multiplier
        ).also {
          annualPriceAdjuster.releaseLockForPriceAdjustment(lockId)
        }
      }.onFailure {
        logger.error(
          "Failed price adjustment for $supplier for effective year $effectiveYear and multiplier $multiplier.",
          it
        )

        monitoringService.capture("Failed price adjustment for $supplier for effective year $effectiveYear and multiplier $multiplier.")
      }.onSuccess {
        logger.info("Succeeded price adjustment for $supplier for effective year $effectiveYear and multiplier $multiplier. Total prices adjusted $it.")
        auditService.create(AuditableEvent.journeyPriceBulkPriceAdjustmentEvent(supplier, effectiveYear, multiplier))
      }
    }
  }
}
