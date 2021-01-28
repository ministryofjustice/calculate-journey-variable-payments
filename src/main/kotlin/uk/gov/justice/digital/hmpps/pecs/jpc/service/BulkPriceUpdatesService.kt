package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.price.nextEffectiveYearForDate
import java.util.UUID

@Service
@Transactional
class BulkPriceUpdatesService(
  private val priceRepository: PriceRepository,
  private val timeSource: TimeSource
) {

  // TODO this operation needs to be audited when the work/code is available.
  fun bulkPriceUpdate(supplier: Supplier, multiplier: Double) {
    val now = timeSource.date()

    priceRepository.deleteBySupplierAndEffectiveYear(supplier, nextEffectiveYearForDate(now))

    priceRepository.flush()

    priceRepository.findBySupplierAndEffectiveYear(supplier, effectiveYearForDate(now)).forEach {
      priceRepository.save(
        it.copy(
          id = UUID.randomUUID(),
          addedAt = timeSource.dateTime(),
          effectiveYear = nextEffectiveYearForDate(now),
          priceInPence = it.price().multiplyBy(multiplier).pence
        )
      )
    }
  }
}
