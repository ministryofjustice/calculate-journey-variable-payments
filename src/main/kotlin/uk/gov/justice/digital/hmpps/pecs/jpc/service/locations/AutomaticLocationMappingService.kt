package uk.gov.justice.digital.hmpps.pecs.jpc.service.locations

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

private val logger = loggerFor<AutomaticLocationMappingService>()

/**
 * Periodically new NOMIS locations are added to book a secure move. This service looks for locations in book a secure
 * move and if they don't exist in the payment service it will automatically create/add them. This saves the users the
 * job of having to do this manually via the payment service UI which in turn means they can focus on the pricing.
 */
@Service
@Transactional
class AutomaticLocationMappingService(
  private val basmClientApi: BasmClientApiService,
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService,
  private val monitoringService: MonitoringService,
) {

  fun mapIfNotPresentLocationsCreatedOn(date: LocalDate) {
    basmClientApi.findNomisAgenciesCreatedOn(date).forEach {
      locationRepository.findByNomisAgencyIdOrSiteName(it.agencyId.uppercase().trim(), it.name.uppercase().trim())
        .let { matchingLocations ->
          when (matchingLocations.size) {
            0 -> {
              locationRepository.save(
                Location(
                  it.locationType,
                  it.agencyId.uppercase().trim(),
                  it.name.uppercase().trim(),
                  timeSource.dateTime(),
                ),
              ).also { newLocation ->
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
