package uk.gov.justice.digital.hmpps.pecs.jpc.service.locations

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.time.ZoneOffset

@Service
@Transactional
class LocationsService(
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  private val auditService: AuditService,
) {

  fun findLocationBySiteName(locationName: String): Location? = locationRepository.findBySiteName(sanitised(locationName))

  fun findAll(): List<Location> = locationRepository.findAll()

  fun getVersion() = locationRepository.findFirstByOrderByUpdatedAtDesc()?.updatedAt?.toEpochSecond(ZoneOffset.UTC) ?: 0

  fun findAgencyLocationAndType(agencyId: String): Triple<String, String, LocationType>? = locationRepository.findByNomisAgencyId(sanitised(agencyId))
    ?.let { Triple(it.nomisAgencyId, it.siteName, it.locationType) }

  fun locationAlreadyExists(agencyId: String, siteName: String) = locationRepository.findBySiteName(sanitised(siteName))
    ?.takeUnless { it.nomisAgencyId == sanitised(agencyId) } != null

  fun setLocationDetails(agencyId: String, friendlyLocationName: String, locationType: LocationType) {
    val sanitizedAgencyId = sanitised(agencyId)
    val sanitisedLocationName = sanitised(friendlyLocationName)

    fun Location.eitherHasChanged(siteName: String, type: LocationType) = this.siteName != siteName || this.locationType != type

    locationRepository.findByNomisAgencyId(sanitizedAgencyId)?.let {
      if (it.eitherHasChanged(sanitisedLocationName, locationType)) {
        val oldLocation = it.copy()
        it.siteName = sanitisedLocationName
        it.locationType = locationType
        it.updatedAt = timeSource.dateTime()

        AuditableEvent.remapLocation(oldLocation, locationRepository.save(it).copy())
          .let { e -> auditService.create(e) }
      }

      return
    }

    AuditableEvent.mapLocation(
      locationRepository.save(
        Location(
          locationType,
          sanitizedAgencyId,
          sanitisedLocationName,
          timeSource.dateTime(),
          timeSource.dateTime(),
        ).copy(),
      ),
    ).let { e -> auditService.create(e) }
  }

  fun locationHistoryForAgencyId(agencyId: String) = auditService.auditEventsByTypeAndMetaKey(AuditEventType.LOCATION, MapLocationMetadata.key(agencyId))
    .associateWith { MapLocationMetadata.map(it) }.keys

  private fun sanitised(value: String) = value.trim().uppercase()
}
