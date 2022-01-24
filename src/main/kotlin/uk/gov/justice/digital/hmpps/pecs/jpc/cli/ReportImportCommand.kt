package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

/**
 * Allows manual running the importing of reporting data from move data that has been uploaded to S3.
 */
private val logger = loggerFor<ReportImportCommand>()

@ConditionalOnNotWebApplication
@Component
class ReportImportCommand(private val importService: ImportService) {

  /**
   * Due to potentially large volumes of data, each day is imported as an individual import to reduce the memory footprint.
   */
  fun importReports(from: LocalDate, to: LocalDate) {
    logger.info("Starting import of reports from $from to $to.")

    importService.importReportsOn(ClosedRangeLocalDate(from, to))

    logger.info("Finished import of reports from $from to $to.")
  }
}
