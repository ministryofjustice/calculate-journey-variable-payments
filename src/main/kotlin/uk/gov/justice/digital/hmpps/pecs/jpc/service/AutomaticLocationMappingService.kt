package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import java.time.LocalDate

@Service
@Transactional
class AutomaticLocationMappingService(
  private val basmClientApi: BasmClientApiService,
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService,
  private val monitoringService: MonitoringService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun mapIfNotPresentLocationsCreatedOn(date: LocalDate) {
    basmClientApi.findNomisAgenciesCreatedOn(date).forEach {
      locationRepository.findByNomisAgencyIdOrSiteName(it.agencyId.uppercase().trim(), it.name.uppercase().trim()).let { location ->
        when (location.size) {
          0 -> {
            locationRepository.save(Location(it.locationType, it.agencyId.uppercase().trim(), it.name.uppercase().trim(), timeSource.dateTime())).also { newLocation ->
              auditService.create(AuditableEvent.autoMapLocation(newLocation))

              logger.info("Automatically mapped new location: agency ID '${it.agencyId}', name '${it.name}' and type '${it.locationType.label}'")
            }
          }
          1 -> logger.info("Location with agency id '${it.agencyId}' and name '${it.name}' already exists, no mapping required.")
          else -> {
            logger.warn("Multiple locations for agency id '${it.agencyId}' and location name '${it.name}', raise with end users.")
            monitoringService.capture("Multiple locations for agency id '${it.agencyId}' and location name '${it.name}', raise with end users.")
          }
        }
      }
    }
  }
}
