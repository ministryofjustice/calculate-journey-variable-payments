package uk.gov.justice.digital.hmpps.pecs.jpc.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceUplifter
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

/**
 * Service to handle (annual) contractual price uplifts for suppliers in relation to inflationary changes.
 *
 * Generally speaking but not always uplifts mainly take place in at the start of the new contractual year e.g. September.
 */
@Service
@Transactional
class PriceUpliftService(
  private val priceUplifter: PriceUplifter,
  private val monitoringService: MonitoringService,
  private val auditService: AuditService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun uplift(supplier: Supplier, effectiveYear: Int, multiplier: Double) = runBlocking {
    launch {
      priceUplifter.uplift(
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
          auditService.create(AuditableEvent.journeyPriceBulkUpdateEvent(supplier, effectiveYear, multiplier))
        }
      )
    }
  }
}
