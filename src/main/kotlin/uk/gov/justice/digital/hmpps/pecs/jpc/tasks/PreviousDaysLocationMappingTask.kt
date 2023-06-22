package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.AutomaticLocationMappingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * This task maps new locations added to BaSM for the previous day to that of the date of execution.
 */
private val logger = loggerFor<PreviousDaysLocationMappingTask>()

@Component
class PreviousDaysLocationMappingTask(
  private val service: AutomaticLocationMappingService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService,
) : Task("Previous days locations", monitoringService) {

  override fun performTask() {
    timeSource.yesterday().run {
      logger.info("Mapping previous days locations added to BaSM (if any): $this.")

      service.mapIfNotPresentLocationsCreatedOn(this)

      logger.info("Finished mapping previous days locations added to BaSM (if any): $this.")
    }
  }
}
