package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportPricesService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * Allows manual running the bulk import of supplier journey prices. Note it will base the prices on the Schedule 34
 * spreadsheets uploaded to S3 in the environment this is being run.
 */
private val logger = loggerFor<BulkPriceImportCommand>()

@ConditionalOnNotWebApplication
@Component
class BulkPriceImportCommand(private val importService: ImportPricesService, private val effectiveYear: EffectiveYear) {

  fun bulkImportPricesFor(supplier: Supplier, year: Int) {
    if (!effectiveYear.canAddOrUpdatePrices(year))
      throw RuntimeException(
        "Price imports can only take place in the current '${effectiveYear.current()}' or previous effective year '${effectiveYear.current() - 1}'."
      )

    logger.info("Starting import of prices for $supplier for effective year $year.")

    when (supplier) {
      Supplier.UNKNOWN -> throw RuntimeException("UNKNOWN is not a valid supplier")
      else -> importService.importPricesFor(supplier, year)
    }

    logger.info("Finished import of prices for $supplier for effective year $year.")
  }
}
