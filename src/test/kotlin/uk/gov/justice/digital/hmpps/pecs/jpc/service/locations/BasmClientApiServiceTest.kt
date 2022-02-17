package uk.gov.justice.digital.hmpps.pecs.jpc.service.locations

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmNomisLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

internal class BasmClientApiServiceTest {

  private val basmApiServer = MockWebServer()

  private val basmApiWebClient = WebClient.builder().baseUrl("http://localhost:${basmApiServer.port}").build()

  private val monitoringService: MonitoringService = mock()

  private val service = BasmClientApiService(basmApiWebClient, monitoringService, Duration.ofMillis(500))

  @Test
  internal fun `location name is found for agency COURT1`() {
    basmApiServer.enqueue(locationResponse("Court One"))

    val location = service.findNomisAgencyLocationNameBy("court1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT1")
    assertThat(location).isEqualTo("COURT ONE")
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency COURT2`() {
    basmApiServer.enqueue(locationResponse("Court Two"))

    val location = service.findNomisAgencyLocationNameBy("court2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT2")
    assertThat(location).isEqualTo("COURT TWO")
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON1`() {
    basmApiServer.enqueue(locationResponse("Prison One"))

    val location = service.findNomisAgencyLocationNameBy("prison1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON1")
    assertThat(location).isEqualTo("PRISON ONE")
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON2`() {
    basmApiServer.enqueue(locationResponse("Prison Two"))

    val location = service.findNomisAgencyLocationNameBy("prison2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON2")
    assertThat(location).isEqualTo("PRISON TWO")
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when location name not found`() {
    basmApiServer.enqueue(locationResponse(body = "{ \"data\": [] }"))

    assertThat(service.findNomisAgencyLocationNameBy("NO MATCH")).isNull()
    verify(monitoringService).capture(any())
  }

  @Test
  internal fun `monitoring service is called with location lookup call timeout`() {
    basmApiServer.enqueue(locationResponse("timed out").setBodyDelay(1, TimeUnit.SECONDS))

    assertThat(service.findNomisAgencyLocationNameBy("TIMEOUT")).isNull()
    verify(monitoringService).capture(any())
  }

  @ParameterizedTest
  @MethodSource("locationMappingTestData")
  fun `locations types are mapped to the correct type`(input: FakeNomisLocation, expected: BasmNomisLocation?) {
    basmApiServer.enqueue(locationResponse(input.title, input.nomisAgencyId, input.locationType, input.createdAt))

    val mappedLocation = service.findNomisAgenciesCreatedOn(input.createdAt)

    assertThat(mappedLocation).containsExactly(expected)
    verifyNoInteractions(monitoringService)
  }

  @Test
  fun `unrecognised locations are not mapped`() {
    basmApiServer.enqueue(locationResponse(" unknown ", " court_agency_ID ", "unknown_type", LocalDate.of(2021, 5, 14)))

    assertThat(service.findNomisAgenciesCreatedOn(LocalDate.of(2021, 5, 14))).isEmpty()
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called with getting created location call timeout`() {
    basmApiServer.enqueue(locationResponse("timed out").setBodyDelay(1, TimeUnit.SECONDS))

    assertThat(service.findNomisAgenciesCreatedOn(LocalDate.now())).isEmpty()
    verify(monitoringService).capture(any())
  }

  private companion object {
    @JvmStatic
    fun locationMappingTestData(): List<Arguments> = listOf(
      Arguments.of(
        FakeNomisLocation(" Approved ", " Approved_agency_ID ", "approved_premises", LocalDate.of(2021, 5, 1)),
        BasmNomisLocation("APPROVED", "APPROVED_AGENCY_ID", LocationType.APP)
      ),
      Arguments.of(
        FakeNomisLocation(" Hospital ", " hospital_agency_ID ", "hospital", LocalDate.of(2021, 5, 2)),
        BasmNomisLocation("HOSPITAL", "HOSPITAL_AGENCY_ID", LocationType.HP)
      ),
      Arguments.of(
        FakeNomisLocation(" Police ", " police_agency_ID ", "police", LocalDate.of(2021, 5, 3)),
        BasmNomisLocation("POLICE", "POLICE_AGENCY_ID", LocationType.PS)
      ),
      Arguments.of(
        FakeNomisLocation(" Prison ", " prison_agency_ID ", "prison", LocalDate.of(2021, 5, 4)),
        BasmNomisLocation("PRISON", "PRISON_AGENCY_ID", LocationType.PR)
      ),
      Arguments.of(
        FakeNomisLocation(" Probation ", " probation_agency_ID ", "probation_office", LocalDate.of(2021, 5, 5)),
        BasmNomisLocation("PROBATION", "PROBATION_AGENCY_ID", LocationType.PB)
      ),
      Arguments.of(
        FakeNomisLocation(
          " Immigration ",
          " immigration_agency_ID ",
          "immigration_detention_centre",
          LocalDate.of(2021, 5, 6)
        ),
        BasmNomisLocation("IMMIGRATION", "IMMIGRATION_AGENCY_ID", LocationType.IM)
      ),
      Arguments.of(
        FakeNomisLocation(
          " High Security Hospital ",
          " high_security_hospital_agency_ID ",
          "high_security_hospital",
          LocalDate.of(2021, 5, 7)
        ),
        BasmNomisLocation("HIGH SECURITY HOSPITAL", "HIGH_SECURITY_HOSPITAL_AGENCY_ID", LocationType.HP)
      ),
      Arguments.of(
        FakeNomisLocation(" sch ", " sch_agency_ID ", "secure_childrens_home", LocalDate.of(2021, 5, 8)),
        BasmNomisLocation("SCH", "SCH_AGENCY_ID", LocationType.SCH)
      ),
      Arguments.of(
        FakeNomisLocation(" stc ", " stc_agency_ID ", "secure_training_centre", LocalDate.of(2021, 5, 9)),
        BasmNomisLocation("STC", "STC_AGENCY_ID", LocationType.STC)
      ),
      Arguments.of(
        FakeNomisLocation(" County courT ", " court_agency_ID ", "court", LocalDate.of(2021, 5, 10)),
        BasmNomisLocation("COUNTY COURT", "COURT_AGENCY_ID", LocationType.CO)
      ),
      Arguments.of(
        FakeNomisLocation(" combineD courT ", " court_agency_ID ", "court", LocalDate.of(2021, 5, 11)),
        BasmNomisLocation("COMBINED COURT", "COURT_AGENCY_ID", LocationType.CM)
      ),
      Arguments.of(
        FakeNomisLocation(" cRown courT ", " court_agency_ID ", "court", LocalDate.of(2021, 5, 12)),
        BasmNomisLocation("CROWN COURT", "COURT_AGENCY_ID", LocationType.CC)
      ),
      Arguments.of(
        FakeNomisLocation(" magistrates courT ", " court_agency_ID ", "court", LocalDate.of(2021, 5, 13)),
        BasmNomisLocation("MAGISTRATES COURT", "COURT_AGENCY_ID", LocationType.MC)
      ),
      Arguments.of(
        FakeNomisLocation(" ranDom courT ", " court_agency_ID ", "court", LocalDate.of(2021, 5, 14)),
        BasmNomisLocation("RANDOM COURT", "COURT_AGENCY_ID", LocationType.CRT)
      )
    )
  }

  data class FakeNomisLocation(
    val title: String,
    val nomisAgencyId: String,
    val locationType: String,
    val createdAt: LocalDate
  )

  private fun locationResponse(
    title: String = "",
    nomisAgencyId: String = "",
    locationType: String = "police",
    createdAt: LocalDate = LocalDate.now(),
    body: String = """
      { "data": [ 
          { 
            "attributes": {
               "title": "$title",
               "nomis_agency_id": "$nomisAgencyId",
               "location_type": "$locationType",
               "created_at": "$createdAt"
            }
          } 
        ]
      }"""
  ) =
    MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody(body)
}
