package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
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
  private val actualEffectiveYear: EffectiveYear,
  private val jobRunner: JobRunner
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Price adjustments cannot be before the current effective year, if the supplied effective year is before it then an
   * exception will be thrown.
   *
   * An adjustment normally takes place at the start of the effective year is based around inflationary price rises.
   */
  fun adjust(
    supplier: Supplier,
    suppliedEffective: Int,
    // TODO multiplier needs to be changed to a value object
    multiplier: Double,
    authentication: Authentication?,
    details: String
  ) {
    if (suppliedEffective < actualEffectiveYear.current()) {
      throw RuntimeException("Price adjustments cannot be before the current effective year ${actualEffectiveYear.current()}.")
    }

    if (multiplier >= 10) throw RuntimeException("Max allowed multiplier exceeded.")

    logger.info("Starting price adjustment for $supplier for effective year $suppliedEffective using multiplier $multiplier.")

    doAdjustment(supplier, suppliedEffective, multiplier, authentication, details)

    logger.info("Running price adjustment for $supplier for effective year $suppliedEffective using multiplier $multiplier.")
  }

  private fun doAdjustment(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: Double,
    authentication: Authentication?,
    details: String
  ) {
    val lockId = annualPriceAdjuster.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)

    jobRunner.run("annual price adjustment") {
      Result.runCatching {
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
        auditService.create(
          AuditableEvent.journeyPriceBulkPriceAdjustmentEvent(
            supplier,
            effectiveYear,
            multiplier,
            authentication,
            details
          )
        )
      }
    }
  }

  fun adjustmentsHistoryFor(supplier: Supplier) =
    auditService.auditEventsByTypeAndMetaKey(AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT, supplier.name)

  fun adjustmentInProgressFor(supplier: Supplier) = annualPriceAdjuster.isInProgressFor(supplier)
}
