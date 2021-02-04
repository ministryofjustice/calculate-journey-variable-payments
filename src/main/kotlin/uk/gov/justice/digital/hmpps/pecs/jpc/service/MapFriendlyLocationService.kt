package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

@Service
@Transactional
class MapFriendlyLocationService(
  private val locationRepository: LocationRepository,
  private val timeSource: TimeSource
) {

  @Autowired
  private lateinit var auditService: AuditService

  fun findAgencyLocationAndType(agencyId: String): Triple<String, String, LocationType>? =
    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())
      ?.let { Triple(it.nomisAgencyId, it.siteName, it.locationType) }

  fun locationAlreadyExists(agencyId: String, siteName: String): Boolean {
    return locationRepository.findBySiteName(siteName.trim().toUpperCase())
      ?.takeUnless { it.nomisAgencyId == agencyId.trim().toUpperCase() } != null
  }

  fun mapFriendlyLocation(agencyId: String, friendlyLocationName: String, locationType: LocationType) {
    val authentication = SecurityContextHolder.getContext().authentication

    locationRepository.findByNomisAgencyId(agencyId.trim().toUpperCase())?.let {
      val oldName = it.siteName
      val oldType = it.locationType
      it.siteName = friendlyLocationName.trim().toUpperCase()
      it.locationType = locationType

      locationRepository.save(it)

      authentication?.let { authentication ->
        if (oldName != it.siteName)
          auditService.createLocationNameChangeEvent(authentication.name, agencyId, oldName, it.siteName)
        if (oldType != it.locationType)
          auditService.createLocationTypeChangeEvent(authentication.name, agencyId, oldType, it.locationType)
      }

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

    authentication?.let {
      auditService.createLocationNameSetEvent(it.name, agencyId, friendlyLocationName.toUpperCase().trim())
      auditService.createLocationTypeSetEvent(it.name, agencyId, locationType)
    }
  }
}
