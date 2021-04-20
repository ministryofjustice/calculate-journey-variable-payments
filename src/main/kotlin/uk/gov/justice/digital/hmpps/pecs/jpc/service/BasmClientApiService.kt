package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class BasmClientApiService(
  private val basmApiWebClient: WebClient,
  private val monitoringService: MonitoringService,
  @Value("\${BASM_API_TIMEOUT:10s}") val basmApiTimeout: Duration
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun findNomisAgencyLocationNameBy(agencyId: String): String? {
    logger.info("Looking up location name for agency ID '${agencyId.trim().toUpperCase()}'.")

    fun recordIfLocationNotFound(location: LocationResponse?) {
      if (location?.data.isNullOrEmpty()) {
        logger.info("Location name for agency ID '${agencyId.trim().toUpperCase()}' not found on calling BaSM API.")
        monitoringService.capture("Location name for agency ID '${agencyId.trim().toUpperCase()}' not found on calling BaSM API.")
      }
    }

    fun recordLocationLookupFailure(error: Throwable) {
      logger.error("An error occurred trying to find location name for agency ID '${agencyId.trim().toUpperCase()}' on calling BaSM API: ${error.message}")
      monitoringService.capture("An error occurred trying to find location name for agency ID '${agencyId.trim().toUpperCase()}' on calling BaSM API: ${error.message}")
    }

    return Result.runCatching {
      basmApiWebClient
        .get()
        .uri("api/reference/locations?filter[nomis_agency_id]=${agencyId.trim().toUpperCase()}")
        .retrieve()
        .bodyToMono(LocationResponse::class.java)
        .block(basmApiTimeout)
    }
      .onFailure { recordLocationLookupFailure(it) }
      .onSuccess { recordIfLocationNotFound(it) }.getOrNull()?.data?.elementAtOrNull(0)?.name()?.toUpperCase()
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
private data class LocationResponse(val data: List<Location> = listOf())

private data class Location(val attributes: Map<Any, Any>) {
  fun name(): String = attributes.getOrDefault("title", "unknown") as String
}
