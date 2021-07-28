package uk.gov.justice.digital.hmpps.pecs.jpc.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.price.AnnualPriceAdjuster
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

/**
 * Service to handle annual price adjustments for suppliers.
 *
 * Generally speaking (but not always) annual adjustments take place at the start of a new effective year e.g. September.
 */
@Service
@Transactional
class AnnualPriceAdjustmentsService(
  private val annualPriceAdjuster: AnnualPriceAdjuster,
  private val monitoringService: MonitoringService,
  private val auditService: AuditService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * An uplift normally takes place at the start of the effective year is based around inflationary price rises.
   */
  fun uplift(supplier: Supplier, effectiveYear: Int, multiplier: Double) = runBlocking {
    launch {
      annualPriceAdjuster.uplift(
        supplier,
        effectiveYear,
        multiplier,
        {
          logger.error(
            "Failed price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier.",
            it
          )

          monitoringService.capture("Failed price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier.")
        },
        {
          logger.info("Succeeded price uplift for $supplier for effective year $effectiveYear and multiplier $multiplier. Total prices uplifted $it.")
          auditService.create(AuditableEvent.journeyPriceBulkUpliftEvent(supplier, effectiveYear, multiplier))
        }
      )
    }
  }
}
