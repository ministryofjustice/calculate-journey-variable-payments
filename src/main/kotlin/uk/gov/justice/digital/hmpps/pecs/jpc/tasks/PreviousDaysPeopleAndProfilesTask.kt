package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<PreviousDaysPeopleAndProfilesTask>()

/**
 * This task imports people and profiles for the previous day to that of the date of execution.
 */
@Component
class PreviousDaysPeopleAndProfilesTask(
  val importService: ImportService,
  val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Previous days people and profiles", monitoringService) {

  override fun performTask() {
    timeSource.yesterday().run {
      logger.info("Importing people and profiles for date $this.")

      importService.importPeopleProfiles(this)

      logger.info("Finished importing people and profiles for date $this.")
    }
  }
}
