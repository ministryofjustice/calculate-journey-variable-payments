package uk.gov.justice.digital.hmpps.pecs.jpc.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

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
   * Price uplifts cannot be before the current effective year, if the supplied effective year is before it then an
   * exception will be thrown.
   */
  fun uplift(supplier: Supplier, suppliedEffective: Int, multiplier: Double) {
    if (suppliedEffective < actualEffectiveYear.current()) {
      throw RuntimeException("Price uplifts cannot be before the current effective year ${actualEffectiveYear.current()}.")
    }

    logger.info("Starting price uplift for $supplier for effective year $suppliedEffective using multiplier $multiplier.")

    doUplift(supplier, suppliedEffective, multiplier)
  }

  /**
   * An uplift normally takes place at the start of the effective year is based around inflationary price rises.
   */
  private fun doUplift(supplier: Supplier, effectiveYear: Int, multiplier: Double) = runBlocking {
    launch {
      Result.runCatching {
        val lockId = annualPriceAdjuster.attemptLockForPriceAdjustment(supplier)

        annualPriceAdjuster.uplift(
          lockId,
          supplier,
          effectiveYear,
          multiplier
        ).also {
          annualPriceAdjuster.releaseLockForPriceAdjustment(lockId)
        }
      }.onFailure {
        logger.error(
          "Failed price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier.",
          it
        )

        monitoringService.capture("Failed price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier.")
      }.onSuccess {
        logger.info("Succeeded price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier. Total prices uplifted $it.")
        auditService.create(AuditableEvent.journeyPriceBulkUpliftEvent(supplier, effectiveYear, multiplier))
      }
    }
  }
}
