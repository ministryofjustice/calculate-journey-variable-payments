package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * This enables us to backfill all the move data from the start of the actual contract (September 2020) to the day prior
 * to the date of execution when the task is enabled.
 *
 * Caution: this task should only be enabled when needed and set to run on a specific day and month and out of hours.
 * Once complete the task should be disabled.
 */
@Component
class BackfillReportsTask(
  private val importService: ImportService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Backfill reports", monitoringService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  private val contractStartDate = LocalDate.of(2020, 9, 1)

  override fun performTask() {
    val now = timeSource.dateTime()

    val yesterday = timeSource.yesterday()

    logger.info("Starting the reports backfill for the date range $contractStartDate to $yesterday")

    importService.importReportsOn(ClosedRangeLocalDate(contractStartDate, timeSource.date().minusDays(1)))

    logger.info("Completed the reports backfill for the date range $contractStartDate to ${now.until(timeSource.dateTime(), ChronoUnit.SECONDS)}")
  }

  private fun TimeSource.yesterday() = this.date().minusDays(1)
}
