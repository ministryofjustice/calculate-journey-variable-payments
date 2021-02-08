package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

@Service
@Transactional
class MapFriendlyLocationService(
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource,
  @Autowired private val auditService: AuditService
) {
  fun findAgencyLocationAndType(agencyId: String): Triple<String, String, LocationType>? =
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
      ?.let { Triple(it.nomisAgencyId, it.siteName, it.locationType) }

  fun locationAlreadyExists(agencyId: String, siteName: String): Boolean {
    return locationRepository.findBySiteName(siteName.trim().toUpperCase())
      ?.takeUnless { it.nomisAgencyId == agencyId.trim().toUpperCase() } != null
  }

  fun mapFriendlyLocation(agencyId: String, friendlyLocationName: String, locationType: LocationType) {
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())?.let {
      val oldName = it.siteName
      val oldType = it.locationType
      it.siteName = friendlyLocationName.trim().toUpperCase()
      it.locationType = locationType

      locationRepository.save(it)

      if (oldName != it.siteName)
        auditService.create(
          AuditableEvent.createLocationNameChangeEvent(
            agencyId,
            oldName,
            it.siteName
          )
        )
      if (oldType != it.locationType)
        auditService.create(
          AuditableEvent.createLocationTypeChangeEvent(
            agencyId,
            oldType,
            it.locationType
          )
        )

      return
    }

    locationRepository.save(
      Location(
        locationType,
        agencyId.toUpperCase().trim(),
        friendlyLocationName.toUpperCase().trim(),
        timeSource.dateTime()
      )
    )

    auditService.create(
      AuditableEvent.createLocationNameSetEvent(
        agencyId,
        friendlyLocationName.toUpperCase().trim()
      )
    )
    auditService.create(AuditableEvent.createLocationTypeSetEvent(agencyId, locationType))
  }
}
