package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration

/**
 * Service for bulk importing supplier journey prices from AWS S3.
 */
private val logger = loggerFor<ImportPricesService>()

@Service
class ImportPricesService(
  private val priceImporter: PriceImporter,
  private val timeSource: TimeSource
) {
  fun importPricesFor(supplier: Supplier, year: Int, action: PriceImporter.Action? = PriceImporter.Action.ERROR) {
    logger.info("Attempting import of prices")

    val start = timeSource.dateTime()

    try {
      priceImporter.import(supplier, year, action)
    } finally {
      val end = timeSource.dateTime()
      logger.info("Import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
    }
  }
}
