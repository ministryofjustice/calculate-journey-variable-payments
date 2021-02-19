package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * These commands are a temporary measure/solution to allow manually running the import of locations, prices and reporting data.
 */
@ShellComponent
class ImportCommands(
  @Autowired private val importService: ImportService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ShellMethod("Imports prices for the given supplier from S3. This command deletes all existing prices for the given supplier.")
  fun importPrices(supplier: Supplier) {
    logger.info("Starting import of prices for $supplier.")

    when (supplier) {
      Supplier.UNKNOWN -> throw RuntimeException("UNKNOWN is not a valid supplier")
      else -> importService.importPrices(supplier)
    }

    logger.info("Finished import of prices for $supplier.")
  }

  /**
   * Due to potentially large volumes of data, each day is imported as an individual import to reduce the memory footprint.
   */
  @ShellMethod("Imports reports for both suppliers for the given dates. Date params are the in ISO date format e.g. YYYY-MM-DD.")
  fun importReports(from: LocalDate, to: LocalDate) {
    logger.info("Starting import of reports from $from to $to.")

    if (to.isBefore(from)) throw RuntimeException("To date must be equal to or greater than from date.")
    for (i in 0..ChronoUnit.DAYS.between(from, to)) {
      importService.importReportsOn(from.plusDays(i))
    }

    logger.info("Finished import of reports from $from to $to.")
  }
}
