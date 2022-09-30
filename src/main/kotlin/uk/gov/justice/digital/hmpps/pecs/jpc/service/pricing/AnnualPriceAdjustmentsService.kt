package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JobRunner
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * Service to handle annual price adjustments for supplier prices.
 *
 * Annual adjustments take place at the start of a new effective (financial) year. This starts in September and ends in
 * August the following year.
 *
 * There are two types of adjustments, inflationary and volumetric. The inflationary rate and volumetric rate are done
 * at different times. It is important when we do know the volumetric rate we re-apply the inflationary rate as there
 * will highly likely be prices that have been added since then but not had the inflationary rate applied.
 */
private val logger = loggerFor<AnnualPriceAdjustmentsService>()

@Service
class AnnualPriceAdjustmentsService(
  private val annualPriceAdjuster: AnnualPriceAdjuster,
  private val monitoringService: MonitoringService,
  private val auditService: AuditService,
  private val actualEffectiveYear: EffectiveYear,
  private val jobRunner: JobRunner
) {

  private enum class AdjustmentType {
    INFLATION,
    VOLUME
  }

  /**
   * Price adjustments cannot be before the current effective year unless forced, if the supplied effective year is
   * before it and not forced then an exception will be thrown.
   */
  fun adjust(
    supplier: Supplier,
    suppliedEffective: Int,
    inflationary: AdjustmentMultiplier,
    volumetric: AdjustmentMultiplier? = null,
    authentication: Authentication?,
    details: String,
    force: Boolean = false
  ) {
    if (!force && suppliedEffective < actualEffectiveYear.previous()) {
      throw RuntimeException("Price adjustments cannot be before the previous effective year ${actualEffectiveYear.previous()}.")
    }

    inflationary(
      supplier,
      suppliedEffective,
      inflationary,
      authentication,
      details,
      volumetric?.let {
        {
          volumetric(
            supplier,
            suppliedEffective,
            volumetric,
            authentication,
            details
          )
        }
      } ?: { }
    )
  }

  /**
   * An inflationary based adjustment takes place at the start of the effective year.
   */
  private fun inflationary(
    supplier: Supplier,
    suppliedEffective: Int,
    multiplier: AdjustmentMultiplier,
    authentication: Authentication?,
    details: String,
    callback: () -> Unit = { }
  ) {
    logger.info("Starting inflationary price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")

    doAdjustment(
      supplier,
      suppliedEffective,
      multiplier,
      authentication,
      details,
      AdjustmentType.INFLATION,
      callback
    )

    logger.info("Running inflationary price adjustment for $supplier for effective year $suppliedEffective using multiplier ${multiplier.value}.")
  }

  /**
   * A volumetric based adjustment takes place at the start of the effective year after the inflationary adjustment.
   */
  private fun volumetric(
    supplier: Supplier,
    suppliedEffective: Int,
    multiplier: AdjustmentMultiplier,
    authentication: Authentication?,
    details: String
  ) {
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
    type: AdjustmentType,
    callback: () -> Unit = { }
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
            callback.invoke()
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
