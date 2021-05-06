package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.time.Duration
import java.time.LocalDate

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
      if (location?.locations.isNullOrEmpty()) {
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
      .onSuccess { recordIfLocationNotFound(it) }.getOrNull()?.locations?.elementAtOrNull(0)?.name?.toUpperCase()
  }

  fun findNomisAgenciesCreatedOn(date: LocalDate): List<BasmNomisLocation?> =
    // TODO need to record any lookup failures in the onFailure { ... }
    Result.runCatching {
      basmApiWebClient
        .get()
        .uri("api/reference/locations?filter[created_at]=$date")
        .retrieve()
        .bodyToMono(LocationResponse::class.java)
        .map { it.locations }
        .block(basmApiTimeout)
    }.getOrDefault(listOf())
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocationResponse(
  @JsonProperty("data")
  @JsonDeserialize(contentUsing = NomisLocationDeserializer::class)
  val locations: List<BasmNomisLocation> = listOf()
)

data class BasmNomisLocation(
  val name: String,
  val agencyId: String,
  val locationType: LocationType,
  val createdAt: LocalDate
)

object NomisLocationDeserializer : JsonDeserializer<BasmNomisLocation>() {
  override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BasmNomisLocation? =
    (p?.readValueAsTree() as JsonNode)["attributes"]?.let { attributes ->
      val title = attributes["title"].asText().trim().toUpperCase()
      val agencyId = attributes["nomis_agency_id"].asText().trim().toUpperCase()
      val locationType = attributes["location_type"].asText()
      val createdAt = LocalDate.parse(attributes["created_at"].asText())

      return LocationTypeParser.parse(locationType)?.let { BasmNomisLocation(title, agencyId, it, createdAt) }
    }
}

object LocationTypeParser {
  fun parse(value: String): LocationType? =
    when (value) {
      "approved_premises" -> LocationType.APP
      "hospital" -> LocationType.HP
      "immigration_detention_centre" -> LocationType.IM
      "high_security_hospital" -> LocationType.HP
      "police" -> LocationType.PS
      "prison" -> LocationType.PR
      "probation_office" -> LocationType.PB
      "secure_childrens_home" -> LocationType.SCH
      "secure_training_centre" -> LocationType.STC
      else -> null
    }
}
