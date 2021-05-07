package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.time.LocalDateTime

internal class BasmAutomaticLocationMappingServiceTest {

  private val fixedTime = LocalDateTime.of(2021, 5, 7, 0, 0)
  private val basmClientApiService: BasmClientApiService = mock()
  private val locationRepository: LocationRepository = mock()
  private val timeSource: TimeSource = TimeSource { fixedTime }
  private val auditService: AuditService = mock()
  private val newLocationCaptor = argumentCaptor<Location>()
  private val service: BasmAutomaticLocationMappingService = BasmAutomaticLocationMappingService(basmClientApiService, locationRepository, timeSource, auditService)

  @Test
  fun `when there are no locations to map there should be basm interactions but no repository or audit interactions`() {
    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(emptyList())

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verifyZeroInteractions(locationRepository)
    verifyZeroInteractions(auditService)
  }

  @Test
  fun `when there is a location to map there should be basm, repository and audit interactions`() {
    val basmLocation = BasmNomisLocation("name", "agency_id", LocationType.CRT, fixedTime.toLocalDate())

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(basmLocation))

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository).save(newLocationCaptor.capture())

    val newLocation = newLocationCaptor.firstValue

    assertThat(newLocation.siteName).isEqualTo("name")
    assertThat(newLocation.nomisAgencyId).isEqualTo("agency_id")
    assertThat(newLocation.locationType).isEqualTo(LocationType.CRT)
    assertThat(newLocation.addedAt).isEqualTo(fixedTime)

    // TODO need to check audit interactions when changes to that have been made
  }

  @Test
  fun `when there multiple locations to map there should be basm, repository and audit interactions`() {
    val basmLocationOne = BasmNomisLocation("one", "agency_one_id", LocationType.CRT, fixedTime.toLocalDate())
    val basmLocationTwo = BasmNomisLocation("two", "agency_two_id", LocationType.PB, fixedTime.toLocalDate())

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(basmLocationOne, basmLocationTwo))

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository, times(2)).save(newLocationCaptor.capture())

    val newLocationOne = newLocationCaptor.firstValue

    assertThat(newLocationOne.siteName).isEqualTo("one")
    assertThat(newLocationOne.nomisAgencyId).isEqualTo("agency_one_id")
    assertThat(newLocationOne.locationType).isEqualTo(LocationType.CRT)
    assertThat(newLocationOne.addedAt).isEqualTo(fixedTime)

    val newLocationTwo = newLocationCaptor.secondValue

    assertThat(newLocationTwo.siteName).isEqualTo("two")
    assertThat(newLocationTwo.nomisAgencyId).isEqualTo("agency_two_id")
    assertThat(newLocationTwo.locationType).isEqualTo(LocationType.PB)
    assertThat(newLocationTwo.addedAt).isEqualTo(fixedTime)

    // TODO need to check audit interactions when changes to that have been made
  }

  @Test
  fun `when there is a duplicate location it should be ignored and there should be basm and repository interactions only`() {
    val duplicateBasmLocation = BasmNomisLocation("name", "agency_id", LocationType.CRT, fixedTime.toLocalDate())
    val duplicateLocation = Location(duplicateBasmLocation.locationType, duplicateBasmLocation.agencyId, duplicateBasmLocation.name)

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(duplicateBasmLocation))
    whenever(locationRepository.findByNomisAgencyId(duplicateBasmLocation.agencyId)).thenReturn(duplicateLocation)

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository).findByNomisAgencyId(duplicateBasmLocation.agencyId)
    verify(locationRepository, never()).save(any())
    verifyZeroInteractions(auditService)
  }
}
