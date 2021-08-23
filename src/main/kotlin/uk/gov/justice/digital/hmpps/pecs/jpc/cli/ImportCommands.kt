package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
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

  @ShellMethod("Simple command to test Sentry IO is enabled. Sends the supplied message to the JPC project issues if enabled.")
  fun sentry(message: String) {
    if (Sentry.isEnabled()) Sentry.captureMessage(message.take(255)) else throw RuntimeException("Sentry not enabled.")
  }
}
