package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * This task imports moves for the previous day to that of the date of execution.
 */
private val logger = loggerFor<PreviousDaysMovesTask>()

@Component
class PreviousDaysMovesTask(
  private val service: ImportService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Previous days moves", monitoringService) {

  override fun performTask() {
    timeSource.yesterday().run {
      logger.info("Importing previous days moves for date $this.")

      service.importMoves(this)

      logger.info("Finished importing previous days moves for date $this.")
    }
  }
}
