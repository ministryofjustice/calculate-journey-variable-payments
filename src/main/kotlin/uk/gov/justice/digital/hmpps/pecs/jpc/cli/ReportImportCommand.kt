package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportReportsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Allows manual running the importing of reporting data from move data that has been uploaded to S3.
 */
private val logger = loggerFor<ReportImportCommand>()

@ConditionalOnNotWebApplication
@Component
class ReportImportCommand(private val importReportsService: ImportReportsService) {

  /**
   * Due to potentially large volumes of data, each day is imported as an individual import to reduce the memory footprint.
   */
  fun importReports(from: LocalDate, to: LocalDate) {
    logger.info("Starting import of reports from $from to $to.")

    DateRange(from, to).run {
      for (i in 0..ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
        importReportsService.importAllReportsOn(this.start.plusDays(i))
      }
    }

    logger.info("Finished import of reports from $from to $to.")
  }

  /**
   * This will import all people and profiles from the date specified to the current date - 1.
   */
  fun importPeopleAndProfiles(from: LocalDate) {
    logger.info("Starting backfill of people and profiles.")

    Result.runCatching {
      importReportsService.importPeopleProfileReportsStartingFrom(from)
    }.onFailure {
      logger.error("An error occurred during import people and profiles", it)
    }

    logger.info("Finished backfill of people and profiles.")
  }
}
