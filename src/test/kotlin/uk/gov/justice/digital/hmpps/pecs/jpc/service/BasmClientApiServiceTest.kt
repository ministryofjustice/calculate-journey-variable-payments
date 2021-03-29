package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class BasmClientApiServiceTest {

  private val basmApiServer = MockWebServer()

  private val basmApiWebClient = WebClient.builder().baseUrl("http://localhost:${basmApiServer.port}").build()

  private val monitoringService: MonitoringService = mock()

  private val service = BasmClientApiService(basmApiWebClient, monitoringService, Duration.ofMillis(500))

  @Test
  internal fun `location name is found for agency COURT1`() {
    basmApiServer.enqueue(locationResponse("Court One"))

    val location = service.findAgencyLocationNameBy("court1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT1")
    assertThat(location).isEqualTo("COURT ONE")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency COURT2`() {
    basmApiServer.enqueue(locationResponse("Court Two"))

    val location = service.findAgencyLocationNameBy("court2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=COURT2")
    assertThat(location).isEqualTo("COURT TWO")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON1`() {
    basmApiServer.enqueue(locationResponse("Prison One"))

    val location = service.findAgencyLocationNameBy("prison1")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON1")
    assertThat(location).isEqualTo("PRISON ONE")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `location name is found for agency PRISON2`() {
    basmApiServer.enqueue(locationResponse("Prison Two"))

    val location = service.findAgencyLocationNameBy("prison2")
    val request = basmApiServer.takeRequest()

    assertThat(request.path).isEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=PRISON2")
    assertThat(location).isEqualTo("PRISON TWO")
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when location when name not found`() {
    basmApiServer.enqueue(locationResponse(body = "{ \"data\": [] }"))

    assertThat(service.findAgencyLocationNameBy("NO MATCH")).isNull()
    verify(monitoringService).capture(any())
  }

  @Test
  internal fun `monitoring service is called on with call timeout`() {
    basmApiServer.enqueue(locationResponse("timed out").setBodyDelay(1, TimeUnit.SECONDS))

    assertThat(service.findAgencyLocationNameBy("TIMEOUT")).isNull()
    verify(monitoringService).capture(any())
  }

  private fun locationResponse(locationName: String? = null, body: String = "{ \"data\": [ { \"attributes\": { \"title\": \"$locationName\" } } ] }") =
    MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody(body)
}
