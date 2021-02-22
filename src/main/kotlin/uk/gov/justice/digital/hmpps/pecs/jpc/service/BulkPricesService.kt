package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.price.nextEffectiveYearForDate
import java.util.UUID

@Service
@Transactional
/**
 * Service to handle bulk price related operations.
 */
class BulkPricesService(
  private val priceRepository: PriceRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun addNextYearsPrices(supplier: Supplier, multiplier: Double) {
    val now = timeSource.date()

    logger.info("Adding $supplier prices for effective year ${nextEffectiveYearForDate(now)} with multiplier $multiplier")

    priceRepository.deleteBySupplierAndEffectiveYear(supplier, nextEffectiveYearForDate(now))

    priceRepository.flush()

    val total = priceRepository.findBySupplierAndEffectiveYear(supplier, effectiveYearForDate(now)).map {
      priceRepository.save(
        it.copy(
          id = UUID.randomUUID(),
          addedAt = timeSource.dateTime(),
          effectiveYear = nextEffectiveYearForDate(now),
          priceInPence = it.price().times(multiplier).pence
        )
      )
    }.count()

    logger.info("$total $supplier prices added for effective year ${nextEffectiveYearForDate(now)}")

    auditService.create(AuditableEvent.journeyPriceBulkUpdateEvent(supplier, multiplier))
  }
}
