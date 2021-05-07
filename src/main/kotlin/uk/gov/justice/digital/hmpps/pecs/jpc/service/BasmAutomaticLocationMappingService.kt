package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.time.LocalDate

@Service
@Transactional
internal class BasmAutomaticLocationMappingService(
  private val basmClientApi: BasmClientApiService,
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService
) {

  fun mapIfNotPresentLocationsCreatedOn(date: LocalDate) {
    basmClientApi.findNomisAgenciesCreatedOn(date).forEach {
      locationRepository.findByNomisAgencyId(it.agencyId).let { location ->
        if (location == null) {
          locationRepository.save(Location(it.locationType, it.agencyId, it.name, timeSource.dateTime()))

          // TODO audit it!
        }
      }
    }
  }
}
