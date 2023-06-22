package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.temporal.ChronoUnit

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
  private val importReportsService: ImportReportsService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService,
) : Task("Backfill reports", monitoringService) {

  override fun performTask() {
    DateRange(EffectiveYear.startOfContract(), timeSource.yesterday()).run {
      val startTime = timeSource.dateTime()

      logger.info("Starting the reports backfill for the period ${this.start} to ${this.endInclusive} at $startTime.")

      for (i in 0..ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
        importReportsService.importAllReportsOn(this.start.plusDays(i))
      }

      logger.info("Completed the reports backfill for the period ${this.start} to ${this.endInclusive}. Started at $startTime and finished at ${timeSource.dateTime()}")
    }
  }
}
