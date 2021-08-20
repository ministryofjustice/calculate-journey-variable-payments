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
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import java.time.LocalDateTime

internal class AutomaticLocationMappingServiceTest {

  private val fixedTime = LocalDateTime.of(2021, 5, 7, 0, 0)
  private val basmClientApiService: BasmClientApiService = mock()
  private val locationRepository: LocationRepository = mock()
  private val timeSource: TimeSource = TimeSource { fixedTime }
  private val auditService: AuditService = mock()
  private val monitoringService: MonitoringService = mock()
  private val newLocationCaptor = argumentCaptor<Location>()
  private val service: AutomaticLocationMappingService = AutomaticLocationMappingService(basmClientApiService, locationRepository, timeSource, auditService, monitoringService)

  @Test
  fun `when there are no locations to map there should be basm interactions but no repository or audit interactions`() {
    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(emptyList())

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verifyZeroInteractions(locationRepository)
    verifyZeroInteractions(auditService)
    verifyZeroInteractions(monitoringService)
  }

  @Test
  fun `when there is a location to map there should be basm, repository and audit interactions`() {
    val basmLocation = BasmNomisLocation(" Name", "agency_iD ", LocationType.CRT)

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(basmLocation))
    whenever(locationRepository.save(any())).thenReturn(Location(LocationType.CRT, "AGENCY_ID", "NAME"))

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository).save(newLocationCaptor.capture())

    val newLocation = newLocationCaptor.firstValue

    assertThat(newLocation.siteName).isEqualTo("NAME")
    assertThat(newLocation.nomisAgencyId).isEqualTo("AGENCY_ID")
    assertThat(newLocation.locationType).isEqualTo(LocationType.CRT)
    assertThat(newLocation.addedAt).isEqualTo(fixedTime)

    verify(auditService).create(AuditableEvent.autoMapLocation(newLocation))
    verifyZeroInteractions(monitoringService)
  }

  @Test
  fun `when there are multiple unique locations to map there should be basm, repository and audit interactions`() {
    val basmLocationOne = BasmNomisLocation("onE ", " Agency_one_id", LocationType.CRT)
    val basmLocationTwo = BasmNomisLocation(" tWo", "agency_Two_id ", LocationType.PB)

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(basmLocationOne, basmLocationTwo))
    whenever(locationRepository.save(any())).thenReturn(
      Location(LocationType.CRT, "AGENCY_ONE_ID", "ONE"),
      Location(LocationType.PB, "AGENCY_TWO_ID", "TWO")
    )

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository, times(2)).save(newLocationCaptor.capture())

    val newLocationOne = newLocationCaptor.firstValue

    assertThat(newLocationOne.siteName).isEqualTo("ONE")
    assertThat(newLocationOne.nomisAgencyId).isEqualTo("AGENCY_ONE_ID")
    assertThat(newLocationOne.locationType).isEqualTo(LocationType.CRT)
    assertThat(newLocationOne.addedAt).isEqualTo(fixedTime)

    val newLocationTwo = newLocationCaptor.secondValue

    assertThat(newLocationTwo.siteName).isEqualTo("TWO")
    assertThat(newLocationTwo.nomisAgencyId).isEqualTo("AGENCY_TWO_ID")
    assertThat(newLocationTwo.locationType).isEqualTo(LocationType.PB)
    assertThat(newLocationTwo.addedAt).isEqualTo(fixedTime)

    verify(auditService).create(AuditableEvent.autoMapLocation(newLocationOne))
    verify(auditService).create(AuditableEvent.autoMapLocation(newLocationTwo))
    verifyZeroInteractions(monitoringService)
  }

  @Test
  fun `when there is a duplicate location it should be ignored and there should be basm and repository interactions only`() {
    val duplicateBasmLocation = BasmNomisLocation(" nAme ", " agency_Id", LocationType.CRT)
    val duplicateLocation = Location(duplicateBasmLocation.locationType, duplicateBasmLocation.agencyId, duplicateBasmLocation.name)

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(duplicateBasmLocation))
    whenever(locationRepository.findByNomisAgencyIdOrSiteName("AGENCY_ID", "NAME")).thenReturn(listOf(duplicateLocation))

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository).findByNomisAgencyIdOrSiteName("AGENCY_ID", "NAME")
    verify(locationRepository, never()).save(any())
    verifyZeroInteractions(auditService)
    verifyZeroInteractions(monitoringService)
  }

  @Test
  fun `when there are multiple location matches in JPC for one BaSM location a Sentry alert should be raised to inform the end users, we cannot resolve this at the application level`() {
    val basmLocation = BasmNomisLocation(" nAme ", " agency_Id", LocationType.CRT)
    val location1 = Location(basmLocation.locationType, basmLocation.agencyId, basmLocation.name)
    val location2 = Location(basmLocation.locationType, basmLocation.agencyId, basmLocation.name)

    whenever(basmClientApiService.findNomisAgenciesCreatedOn(fixedTime.toLocalDate())).thenReturn(listOf(basmLocation))
    whenever(locationRepository.findByNomisAgencyIdOrSiteName("AGENCY_ID", "NAME")).thenReturn(listOf(location1, location2))

    service.mapIfNotPresentLocationsCreatedOn(fixedTime.toLocalDate())

    verify(basmClientApiService).findNomisAgenciesCreatedOn(fixedTime.toLocalDate())
    verify(locationRepository).findByNomisAgencyIdOrSiteName("AGENCY_ID", "NAME")
    verify(locationRepository, never()).save(any())
    verifyZeroInteractions(auditService)
    verify(monitoringService).capture(any())
  }
}
