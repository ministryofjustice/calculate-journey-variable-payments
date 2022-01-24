package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * This task is designed in such a way it will always import reporting data for the previous day. This is based on the
 * current date - 1 day at time of execution.
 */
private val logger = loggerFor<PreviousDaysReportsTask>()

@Component
class PreviousDaysReportsTask(
  private val service: ImportService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Previous Days Reports", monitoringService) {

  override fun performTask() {
    val yesterday = timeSource.date().minusDays(1)

    logger.info("Importing previous days reporting data: $yesterday.")

    service.importReportsOn(yesterday)
  }
}
