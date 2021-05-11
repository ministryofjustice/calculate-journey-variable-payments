package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AutomaticLocationMappingService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService

/**
 * This task is designed in such a way it will always attempt to map new locations added to BaSM for the previous day.
 * This is based on the current date - 1 day at time of execution.
 */
@Component
class PreviousDaysLocationMappingTask(
  private val service: AutomaticLocationMappingService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Previous Days Locations", monitoringService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun performTask() {
    val yesterday = timeSource.date().minusDays(1)

    logger.info("Mapping previous days locations added to BaSM (if any): $yesterday.")

    service.mapIfNotPresentLocationsCreatedOn(yesterday)
  }
}
