package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

@Service
@Transactional
class MapFriendlyLocationService(private val locationRepository: LocationRepository, private val timeSource: TimeSource) {
  fun mapFriendlyLocation(agencyId: String, friendlyLocationName: String, locationType: LocationType) {
    locationRepository.save(Location(locationType, agencyId.toUpperCase().trim(), friendlyLocationName.toUpperCase().trim(), timeSource.dateTime()))
  }
}
