package uk.gov.justice.digital.hmpps.pecs.jpc.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

internal class BasmClientApiServiceTest {

  private val basmApiServer = MockWebServer()

  private val basmApiWebClient = WebClient.builder().baseUrl("http://localhost:${basmApiServer.port}").build()

  private val service = BasmClientApiService(basmApiWebClient)

  @Test
  internal fun `location name is found for agency COURT1`() {
    basmApiServer.enqueue(locationResponse("Court One"))

    val location = service.getLocationName("court1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT1")
    assertThat(location).isEqualTo("Court One")
  }

  @Test
  internal fun `location name is found for agency COURT2`() {
    basmApiServer.enqueue(locationResponse("Court Two"))

    val location = service.getLocationName("court2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT2")
    assertThat(location).isEqualTo("Court Two")
  }

  @Test
  internal fun `location name is found for agency PRISON1`() {
    basmApiServer.enqueue(locationResponse("Prison One"))

    val location = service.getLocationName("prison1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON1")
    assertThat(location).isEqualTo("Prison One")
  }

  @Test
  internal fun `location name is found for agency PRISON2`() {
    basmApiServer.enqueue(locationResponse("Prison Two"))

    val location = service.getLocationName("prison2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON2")
    assertThat(location).isEqualTo("Prison Two")
  }

  @Test
  internal fun `location name not found returns 'no match'`() {
    basmApiServer.enqueue(
      MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setResponseCode(404)
    )

    assertThat(service.getLocationName("DOES_NOT_EXIST")).isEqualTo("no match")
  }

  private fun locationResponse(locationName: String) =
    MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody("{ \"data\": [ { \"attributes\": { \"title\": \"$locationName\" } } ] }")
}
