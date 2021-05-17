package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.time.LocalDate

@Service
@Transactional
class AutomaticLocationMappingService(
  private val basmClientApi: BasmClientApiService,
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun mapIfNotPresentLocationsCreatedOn(date: LocalDate) {
    basmClientApi.findNomisAgenciesCreatedOn(date).forEach {
      locationRepository.findByNomisAgencyIdOrSiteName(it.agencyId.toUpperCase().trim(), it.name.toUpperCase().trim()).let { location ->
        if (location == null) {
          locationRepository.save(Location(it.locationType, it.agencyId.toUpperCase().trim(), it.name.toUpperCase().trim(), timeSource.dateTime())).also { newLocation ->
            auditService.create(AuditableEvent.autoMapLocation(newLocation))

            logger.info("Automatically mapped new location: agency ID '${it.agencyId}', name '${it.name}' and type '${it.locationType.label}'")
          }
        }
      }
    }
  }
}
