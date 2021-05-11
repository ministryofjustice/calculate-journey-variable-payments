package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService

/**
 * This task is designed in such a way it will always import reporting data for the previous day. This is based on the
 * current date - 1 day at time of execution.
 */
@Component
class PreviousDaysReportsTask(
  private val service: ImportService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Previous Days Reports", monitoringService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun performTask() {
    val yesterday = timeSource.date().minusDays(1)

    logger.info("Importing previous days reporting data: $yesterday.")

    service.importReportsOn(yesterday)
  }
}
