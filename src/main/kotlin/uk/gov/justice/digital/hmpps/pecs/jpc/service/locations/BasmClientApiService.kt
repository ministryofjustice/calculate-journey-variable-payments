package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
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
        .bodyToMono(LocationResponseDto::class.java)
        .map { LocationResponse.fromDto(it) }
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
        .bodyToMono(LocationResponseDto::class.java)
        .map { LocationResponse.fromDto(it).locations.filterNotNull() }
        .block(basmApiTimeout)
    }
      .onFailure { recordFailure(it) }
      .getOrDefault(listOf()) ?: listOf()
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocationResponse(
  @JsonProperty("data")
  val locations: List<BasmNomisLocation?> = listOf(),
) {
  companion object {
    fun fromDto(dto: LocationResponseDto): LocationResponse {
      val locations = dto.data.mapNotNull { item ->
        try {
          val attributes = item.attributes
          val title = attributes.title.trim().uppercase()
          val agencyId = attributes.nomisAgencyId.trim().uppercase()
          val locationType = attributes.locationType

          val mayBe: Map<String, LocationType> = mapOf(
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

          fun mayBeCourt(type: String, title: String): LocationType? = if (type == "court") {
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

          (mayBe[locationType] ?: mayBeCourt(locationType, title))?.let { lt ->
            BasmNomisLocation(title, agencyId, lt)
          }
        } catch (e: Exception) {
          logger.error("Error converting location item: ${e.message}", e)
          null
        }
      }
      return LocationResponse(locations)
    }
  }
}

// DTOs for JSON deserialization
data class LocationResponseDto(
  @JsonProperty("data")
  val data: List<LocationItemDto> = listOf(),
)

data class LocationItemDto(
  @JsonProperty("attributes")
  val attributes: LocationAttributesDto,
)

data class LocationAttributesDto(
  @JsonProperty("title")
  val title: String = "",
  @JsonProperty("nomis_agency_id")
  val nomisAgencyId: String = "",
  @JsonProperty("location_type")
  val locationType: String = "",
)

data class BasmNomisLocation(
  val name: String = "",
  val agencyId: String = "",
  val locationType: LocationType,
)
