package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.time.LocalDateTime

internal class LocationsServiceTest {
  private val auditService: AuditService = mock()
  private val locationRepository: LocationRepository = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }
  private val service: LocationsService = LocationsService(locationRepository, timeSource, auditService)

  @Test
  internal fun `site name already exists when agency different`() {
    whenever(locationRepository.findBySiteName("SITE NAME")).thenReturn(Location(LocationType.AP, "xxx", "SITE NAME"))

    assertThat(service.locationAlreadyExists("agencyId", "site name")).isTrue
  }

  @Test
  internal fun `site name does not already exist when agency same`() {
    whenever(locationRepository.findBySiteName("SITE NAME")).thenReturn(
      Location(
        LocationType.AP,
        "AGENCYID",
        "SITE NAME"
      )
    )

    assertThat(service.locationAlreadyExists("agencyId", "site name")).isFalse
  }
}
