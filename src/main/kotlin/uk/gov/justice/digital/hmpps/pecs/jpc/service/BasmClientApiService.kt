package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
@ConditionalOnWebApplication
class BasmClientApiService(private val basmApiWebClient: WebClient) {

  fun getLocationName(agencyId: String): String {
    val result = basmApiWebClient
      .get()
      .uri("api/reference/locations?filter[nomis_agency_id]=${agencyId.trim().toUpperCase()}")
      .retrieve()
      .bodyToMono(LocationResponse::class.java)
      .onErrorReturn(LocationResponse(listOf(Location(mapOf("title" to "no match")))))
      .block(Duration.ofSeconds(20))

    return result?.data?.first()?.name() ?: "no match"
  }
}

private data class LocationResponse(val data: List<Location>)

private data class Location(val attributes: Map<Any, Any>) {
  fun name(): String = attributes.getOrDefault("title", "unknown") as String
}
