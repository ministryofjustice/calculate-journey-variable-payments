package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.time.ZoneOffset

@Service
@Transactional
class LocationsService(
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService
) {

  fun findLocationBySiteName(locationName: String): Location? = locationRepository.findBySiteName(locationName.trim().toUpperCase())

  fun findAll(): List<Location> = locationRepository.findAll()

  fun getVersion() = locationRepository.findFirstByOrderByUpdatedAtDesc()?.updatedAt?.toEpochSecond(ZoneOffset.UTC) ?: 0

  fun findAgencyLocationAndType(agencyId: String): Triple<String, String, LocationType>? =
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
      ?.let { Triple(it.nomisAgencyId, it.siteName, it.locationType) }

  fun locationAlreadyExists(agencyId: String, siteName: String): Boolean {
    return locationRepository.findBySiteName(siteName.trim().toUpperCase())
      ?.takeUnless { it.nomisAgencyId == agencyId.trim().toUpperCase() } != null
  }

  fun mapFriendlyLocation(agencyId: String, friendlyLocationName: String, locationType: LocationType) {
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())?.let {
      val oldLocation = it.copy()
      it.siteName = friendlyLocationName.trim().toUpperCase()
      it.locationType = locationType
      it.updatedAt = timeSource.dateTime()

      AuditableEvent.locationEvent(
        oldLocation,
        locationRepository.save(it).copy(),
      )?.let { e -> auditService.create(e) }

      return
    }

    AuditableEvent.locationEvent(
      locationRepository.save(
        Location(
          locationType,
          agencyId.toUpperCase().trim(),
          friendlyLocationName.toUpperCase().trim(),
          timeSource.dateTime(),
          timeSource.dateTime()
        ).copy()
      )
    )?.let { e -> auditService.create(e) }
  }
}
