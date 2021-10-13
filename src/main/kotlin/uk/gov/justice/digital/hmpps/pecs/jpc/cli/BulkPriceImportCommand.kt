package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

@ShellComponent
class BulkPriceImportCommand(
  private val importService: ImportService,
  private val effectiveYear: EffectiveYear,
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ShellMethod("Bulk imports prices for the given supplier and effective year from the (latest) supplier prices spreadsheet in S3. ")
  fun bulkImportPricesFor(supplier: Supplier, year: Int) {
    if (!effectiveYear.canAddOrUpdatePrices(year))
      throw RuntimeException(
        "Price imports can only take place in the current '${effectiveYear.current()}' or previous effective year '${effectiveYear.current() - 1}'."
      )

    logger.info("Starting import of prices for $supplier for effective year $year.")

    when (supplier) {
      Supplier.UNKNOWN -> throw RuntimeException("UNKNOWN is not a valid supplier")
      else -> importService.importPrices(supplier, year)
    }

    logger.info("Finished import of prices for $supplier for effective year $year.")
  }
}
