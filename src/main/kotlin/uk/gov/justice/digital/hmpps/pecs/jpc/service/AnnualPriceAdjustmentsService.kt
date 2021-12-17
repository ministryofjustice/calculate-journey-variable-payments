package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

/**
 * Service to handle annual price adjustments for supplier prices. There are two types of adjustments, inflationary and
 * volumetric. The inflationary adjustment comes first followed by the volumetric normally later in the month.
 *
 * Annual adjustments take place at the start of a new effective (financial) year. This starts in September and ends in
 * August the following year.
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

  private enum class AdjustmentType {
    INFLATION,
    VOLUME
  }

  /**
   * Price adjustments cannot be before the current effective year, if the supplied effective year is before it then an
   * exception will be thrown.
   *
   * An inflationary based adjustment takes place at the start of the effective year.
   */
  fun inflationary(
    supplier: Supplier,
    suppliedEffective: Int,
    multiplier: AdjustmentMultiplier,
    authentication: Authentication?,
    details: String
  ) {
    if (suppliedEffective < actualEffectiveYear.current()) {
      throw RuntimeException("Price adjustments cannot be before the current effective year ${actualEffectiveYear.current()}.")
    }

    logger.info("Starting inflationary price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")

    doAdjustment(supplier, suppliedEffective, multiplier, authentication, details, AdjustmentType.INFLATION)

    logger.info("Running inflationary price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")
  }

  /**
   * Price adjustments cannot be before the current effective year, if the supplied effective year is before it then an
   * exception will be thrown.
   *
   * A volumetric based adjustment takes place at the start of the effective year after the inflationary adjustment.
   */
  fun volumetric(
    supplier: Supplier,
    suppliedEffective: Int,
    multiplier: AdjustmentMultiplier,
    authentication: Authentication?,
    details: String
  ) {
    if (suppliedEffective < actualEffectiveYear.current()) {
      throw RuntimeException("Price adjustments cannot be before the current effective year ${actualEffectiveYear.current()}.")
    }

    logger.info("Starting volumetric price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")

    doAdjustment(supplier, suppliedEffective, multiplier, authentication, details, AdjustmentType.VOLUME)

    logger.info("Running volumetric price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")
  }

  private fun doAdjustment(
    supplier: Supplier,
    effectiveYear: Int,
    multiplier: AdjustmentMultiplier,
    authentication: Authentication?,
    details: String,
    type: AdjustmentType
  ) {
    val lockId = annualPriceAdjuster.attemptLockForPriceAdjustment(supplier, multiplier, effectiveYear)

    jobRunner.run("$type price adjustment") {
      Result.runCatching {
        if (type == AdjustmentType.INFLATION)
          annualPriceAdjuster.inflationary(
            lockId,
            supplier,
            effectiveYear,
            multiplier
          ).also {
            annualPriceAdjuster.releaseLockForPriceAdjustment(lockId)
          }
        else
          annualPriceAdjuster.volumetric(
            lockId,
            supplier,
            effectiveYear,
            multiplier
          ).also {
            annualPriceAdjuster.releaseLockForPriceAdjustment(lockId)
          }
      }.onFailure {
        logger.error(
          "Failed $type price adjustment for $supplier for effective year $effectiveYear and multiplier ${multiplier.value}.",
          it
        )

        monitoringService.capture("Failed $type price adjustment for $supplier for effective year $effectiveYear and multiplier ${multiplier.value}.")
      }.onSuccess {
        logger.info("Succeeded $type price adjustment for $supplier for effective year $effectiveYear and multiplier ${multiplier.value}. Total prices adjusted $it.")
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
