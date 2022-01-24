package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

/**
 * This enables us to backfill all the move data from the start of the actual contract (September 2020) to the day prior
 * to the date of execution when the task is enabled.
 *
 * Caution: this task should only be enabled when needed and set to run on a specific day and month and out of hours.
 * Once complete the task should be disabled.
 */
private val logger = loggerFor<BackfillReportsTask>()

@Component
class BackfillReportsTask(
  private val importService: ImportService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Backfill reports", monitoringService) {

  private val contractStartDate = LocalDate.of(2020, 9, 1)

  override fun performTask() {
    ClosedRangeLocalDate(contractStartDate, timeSource.yesterday()).run {
      val startTime = timeSource.dateTime()

      logger.info("Starting the reports backfill for the period ${this.start} to ${this.endInclusive} at $startTime.")

      importService.importReportsOn(this)

      logger.info("Completed the reports backfill for the period ${this.start} to ${this.endInclusive}. Started at $startTime and finished at ${timeSource.dateTime()}")
    }
  }
}
