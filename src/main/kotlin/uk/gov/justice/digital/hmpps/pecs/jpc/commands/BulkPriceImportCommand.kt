package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

@ShellComponent
class BulkPriceImportCommand(
  private val importService: ImportService,
  private val effectiveYear: EffectiveYear,
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ShellMethod("Bulk imports prices for the given supplier and effective year from the (latest) supplier prices spreadsheet in S3.")
  fun bulkImportPricesFor(supplier: Supplier, year: Int) {
    if (year < 2019 || year > effectiveYear.current()) throw RuntimeException("Year cannot be earlier than 2019 or greater than ${effectiveYear.current()}.")

    logger.info("Starting import of prices for $supplier for effective year $year.")

    when (supplier) {
      Supplier.UNKNOWN -> throw RuntimeException("UNKNOWN is not a valid supplier")
      // TODO update service layer to take in the effective year.
      else -> importService.importPrices(supplier)
    }

    logger.info("Finished import of prices for $supplier for effective year $year.")
  }
}
