package uk.gov.justice.digital.hmpps.pecs.jpc.service.locations

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.FakeAuthentication
import java.time.LocalDateTime

@ExtendWith(FakeAuthentication::class)
internal class LocationsServiceTest {
  private val auditService: AuditService = mock()
  private val locationRepository: LocationRepository = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }
  private val locationCaptor = argumentCaptor<Location>()
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
        "SITE NAME",
      ),
    )

    assertThat(service.locationAlreadyExists("agencyId", "site name")).isFalse
  }

  @Test
  internal fun `new location is added and audited`() {
    whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(null)
    whenever(locationRepository.save(any())).thenReturn(Location(LocationType.PR, "AGENCY_ID", "SITE NAME"))

    service.setLocationDetails("agency_iD ", "site name", LocationType.PR)

    verify(locationRepository).save(locationCaptor.capture())

    val newLocation = locationCaptor.firstValue

    assertThat(newLocation.nomisAgencyId).isEqualTo("AGENCY_ID")
    assertThat(newLocation.siteName).isEqualTo("SITE NAME")
    assertThat(newLocation.locationType).isEqualTo(LocationType.PR)

    verify(auditService).create(any())
  }

  @Test
  internal fun `existing location name is updated and audited`() {
    val existingLocation = Location(LocationType.PR, "AGENCY_ID", "SITE NAME")

    whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(existingLocation)
    whenever(locationRepository.save(any())).thenReturn(existingLocation.copy(siteName = "NEW SITE NAME"))

    service.setLocationDetails("agency_iD ", "new site name", LocationType.PR)

    verify(locationRepository).save(locationCaptor.capture())

    val updatedLocation = locationCaptor.firstValue

    assertThat(updatedLocation.nomisAgencyId).isEqualTo("AGENCY_ID")
    assertThat(updatedLocation.siteName).isEqualTo("NEW SITE NAME")
    assertThat(updatedLocation.locationType).isEqualTo(LocationType.PR)

    verify(auditService).create(any())
  }

  @Test
  internal fun `existing location type is updated and audited`() {
    val existingLocation = Location(LocationType.PR, "AGENCY_ID", "SITE NAME")

    whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(existingLocation)
    whenever(locationRepository.save(any())).thenReturn(
      existingLocation.copy(
        locationType = LocationType.MC,
        siteName = "SITE NAME",
      ),
    )

    service.setLocationDetails("agency_iD ", "site name", LocationType.MC)

    verify(locationRepository).save(locationCaptor.capture())

    val updatedLocation = locationCaptor.firstValue

    assertThat(updatedLocation.nomisAgencyId).isEqualTo("AGENCY_ID")
    assertThat(updatedLocation.siteName).isEqualTo("SITE NAME")
    assertThat(updatedLocation.locationType).isEqualTo(LocationType.MC)

    verify(auditService).create(any())
  }

  @Test
  internal fun `attempt to update an existing location with the same details has no effect`() {
    whenever(locationRepository.findByNomisAgencyId("AGENCY_ID")).thenReturn(
      Location(
        LocationType.PR,
        "AGENCY_ID",
        "SITE NAME",
      ),
    )

    service.setLocationDetails("agency_iD ", "site name", LocationType.PR)

    verify(locationRepository, never()).save(any())
    verify(auditService, never()).create(any())
  }
}
