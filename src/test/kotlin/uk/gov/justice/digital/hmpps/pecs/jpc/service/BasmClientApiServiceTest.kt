package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
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
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency COURT2`() {
    basmApiServer.enqueue(locationResponse("Court Two"))

    val location = service.findNomisAgencyLocationNameBy("court2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT2")
    assertThat(location).isEqualTo("COURT TWO")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON1`() {
    basmApiServer.enqueue(locationResponse("Prison One"))

    val location = service.findNomisAgencyLocationNameBy("prison1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON1")
    assertThat(location).isEqualTo("PRISON ONE")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON2`() {
    basmApiServer.enqueue(locationResponse("Prison Two"))

    val location = service.findNomisAgencyLocationNameBy("prison2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON2")
    assertThat(location).isEqualTo("PRISON TWO")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when location when name not found`() {
    basmApiServer.enqueue(locationResponse(body = "{ \"data\": [] }"))

    assertThat(service.findNomisAgencyLocationNameBy("NO MATCH")).isNull()
    verify(monitoringService).capture(any())
  }

  @Test
  internal fun `monitoring service is called on with call timeout`() {
    basmApiServer.enqueue(locationResponse("timed out").setBodyDelay(1, TimeUnit.SECONDS))

    assertThat(service.findNomisAgencyLocationNameBy("TIMEOUT")).isNull()
    verify(monitoringService).capture(any())
  }

  @ParameterizedTest
  @MethodSource("locationMappingTestData")
  fun `locations types are mapped to the correct type`(input: NomisLocationTestData, expected: BasmNomisLocation) {
    basmApiServer.enqueue(locationResponse(input.title, input.nomisAgencyId, input.locationType, input.createdAt))

    val mappedLocation = service.findNomisAgenciesCreatedOn(input.createdAt)

    assertThat(mappedLocation).containsExactly(expected)
  }

  private companion object {
    @JvmStatic
    fun locationMappingTestData(): List<Arguments> = listOf(
      Arguments.of(
        NomisLocationTestData(" Approved ", " Approved_agency_ID ", "approved_premises", LocalDate.of(2021, 5, 1)),
        BasmNomisLocation("APPROVED", "APPROVED_AGENCY_ID", LocationType.APP, LocalDate.of(2021, 5, 1))
      ),
      Arguments.of(
        NomisLocationTestData(" Hospital ", " hospital_agency_ID ", "hospital", LocalDate.of(2021, 5, 2)),
        BasmNomisLocation("HOSPITAL", "HOSPITAL_AGENCY_ID", LocationType.HP, LocalDate.of(2021, 5, 2))
      ),
      Arguments.of(
        NomisLocationTestData(" Police ", " police_agency_ID ", "police", LocalDate.of(2021, 5, 3)),
        BasmNomisLocation("POLICE", "POLICE_AGENCY_ID", LocationType.PS, LocalDate.of(2021, 5, 3))
      ),
      Arguments.of(
        NomisLocationTestData(" Prison ", " prison_agency_ID ", "prison", LocalDate.of(2021, 5, 4)),
        BasmNomisLocation("PRISON", "PRISON_AGENCY_ID", LocationType.PR, LocalDate.of(2021, 5, 4))
      ),
      Arguments.of(
        NomisLocationTestData(" Probation ", " probation_agency_ID ", "probation_office", LocalDate.of(2021, 5, 5)),
        BasmNomisLocation("PROBATION", "PROBATION_AGENCY_ID", LocationType.PB, LocalDate.of(2021, 5, 5))
      ),
      Arguments.of(
        NomisLocationTestData(" Immigration ", " immigration_agency_ID ", "immigration_detention_centre", LocalDate.of(2021, 5, 6)),
        BasmNomisLocation("IMMIGRATION", "IMMIGRATION_AGENCY_ID", LocationType.IM, LocalDate.of(2021, 5, 6))
      ),
      Arguments.of(
        NomisLocationTestData(" High Security Hospital ", " high_security_hospital_agency_ID ", "high_security_hospital", LocalDate.of(2021, 5, 7)),
        BasmNomisLocation("HIGH SECURITY HOSPITAL", "HIGH_SECURITY_HOSPITAL_AGENCY_ID", LocationType.HP, LocalDate.of(2021, 5, 7))
      ),
      Arguments.of(
        NomisLocationTestData(" sch ", " sch_agency_ID ", "secure_childrens_home", LocalDate.of(2021, 5, 8)),
        BasmNomisLocation("SCH", "SCH_AGENCY_ID", LocationType.SCH, LocalDate.of(2021, 5, 8))
      ),
      Arguments.of(
        NomisLocationTestData(" stc ", " stc_agency_ID ", "secure_training_centre", LocalDate.of(2021, 5, 9)),
        BasmNomisLocation("STC", "STC_AGENCY_ID", LocationType.STC, LocalDate.of(2021, 5, 9))
      )
      // TODO need to map the various court types
    )
  }

  data class NomisLocationTestData(val title: String, val nomisAgencyId: String, val locationType: String, val createdAt: LocalDate)

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
