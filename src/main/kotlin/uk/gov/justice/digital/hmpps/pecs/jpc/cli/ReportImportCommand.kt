package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Allows manual running the importing of reporting data from move data that has been uploaded to S3.
 */
@ConditionalOnNotWebApplication
@Component
class ReportImportCommand(private val importService: ImportService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Due to potentially large volumes of data, each day is imported as an individual import to reduce the memory footprint.
   */
  fun importReports(from: LocalDate, to: LocalDate) {
    logger.info("Starting import of reports from $from to $to.")

    if (to.isBefore(from)) throw RuntimeException("To date must be equal to or greater than from date.")
    for (i in 0..ChronoUnit.DAYS.between(from, to)) {
      importService.importReportsOn(from.plusDays(i))
    }

    logger.info("Finished import of reports from $from to $to.")
  }
}
