package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration
import java.time.LocalDate

private val logger = loggerFor<BasmClientApiService>()

/**
 * Acts as an API gateway to the book a secure move API.
 */
@Service
class BasmClientApiService(
  private val basmApiWebClient: WebClient,
  private val monitoringService: MonitoringService,
  @Value("\${BASM_API_TIMEOUT:10s}") val basmApiTimeout: Duration,
) {

  fun findNomisAgencyLocationNameBy(agencyId: String): String? {
    logger.info("Looking up location name for agency ID '${agencyId.trim().uppercase()}'.")

    fun recordIfLocationNotFound(location: LocationResponse?) {
      if (location?.locations.isNullOrEmpty()) {
        logger.info("Location name for agency ID '${agencyId.trim().uppercase()}' not found on calling BaSM API.")
        monitoringService.capture("Location name for agency ID '${agencyId.trim().uppercase()}' not found on calling BaSM API.")
      }
    }

    fun recordLocationLookupFailure(error: Throwable) {
      logger.error("An error occurred trying to find location name for agency ID '${agencyId.trim().uppercase()}' on calling BaSM API: ${error.message}")
      monitoringService.capture("An error occurred trying to find location name for agency ID '${agencyId.trim().uppercase()}' on calling BaSM API: ${error.message}")
    }

    return Result.runCatching {
      basmApiWebClient
        .get()
        .uri("api/reference/locations?filter[nomis_agency_id]=${agencyId.trim().uppercase()}")
        .retrieve()
        .bodyToMono(LocationResponse::class.java)
        .block(basmApiTimeout)
    }
      .onFailure { recordLocationLookupFailure(it) }
      .onSuccess { recordIfLocationNotFound(it) }.getOrNull()?.locations?.elementAtOrNull(0)?.name?.uppercase()
  }

  fun findNomisAgenciesCreatedOn(date: LocalDate): List<BasmNomisLocation> {
    fun recordFailure(error: Throwable) {
      logger.error("An error occurred trying to find location by date '$date' on calling BaSM API: ${error.message}")
      monitoringService.capture("An error occurred trying to find location by date '$date' on calling BaSM API: ${error.message}")
    }

    return Result.runCatching {
      basmApiWebClient
        .get()
        .uri("api/reference/locations?filter[created_at]=$date")
        .retrieve()
        .bodyToMono(LocationResponse::class.java)
        .map { it.locations }
        .block(basmApiTimeout)
    }
      .onFailure { recordFailure(it) }
      .getOrDefault(listOf()).filterNotNull()
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocationResponse(
  @JsonProperty("data")
  @JsonDeserialize(contentUsing = NomisLocationDeserializer::class)
  val locations: List<BasmNomisLocation> = listOf(),
)

data class BasmNomisLocation(
  val name: String,
  val agencyId: String,
  val locationType: LocationType,
)

object NomisLocationDeserializer : JsonDeserializer<BasmNomisLocation>() {

  private val mayBe: Map<String, LocationType> = mapOf(
    "approved_premises" to LocationType.APP,
    "hospital" to LocationType.HP,
    "immigration_detention_centre" to LocationType.IM,
    "high_security_hospital" to LocationType.HP,
    "police" to LocationType.PS,
    "prison" to LocationType.PR,
    "probation_office" to LocationType.PB,
    "secure_childrens_home" to LocationType.SCH,
    "secure_training_centre" to LocationType.STC,
  )

  override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BasmNomisLocation? =
    (p?.readValueAsTree() as JsonNode)["attributes"]?.let { attributes ->
      val title = attributes["title"].asText().trim().uppercase()
      val agencyId = attributes["nomis_agency_id"].asText().trim().uppercase()
      val locationType = attributes["location_type"].asText()

      return (mayBe[locationType] ?: mayBeCourt(locationType, title))?.let { BasmNomisLocation(title, agencyId, it) }
    }

  private fun mayBeCourt(type: String, title: String): LocationType? {
    return if (type == "court") {
      val sanitisedTitle = title.uppercase()

      when {
        sanitisedTitle.contains("COMBINED COURT") -> LocationType.CM
        sanitisedTitle.contains("COUNTY COURT") -> LocationType.CO
        sanitisedTitle.contains("CROWN COURT") -> LocationType.CC
        sanitisedTitle.contains("MAGISTRATES COURT") -> LocationType.MC
        else -> LocationType.CRT
      }
    } else {
      null
    }
  }
}
