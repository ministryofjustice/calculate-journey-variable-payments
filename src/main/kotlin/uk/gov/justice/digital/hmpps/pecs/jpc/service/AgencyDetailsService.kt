package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.NomisReferenceDataProvider
import javax.annotation.PostConstruct

private const val HEADER_ROW = 0
private const val NOMIS_AGENCY_ID = 0
private const val NAME = 1

@Service
@Deprecated(message = "This has been superseded with calls the BaSM backend API", level = DeprecationLevel.WARNING)
class AgencyDetailsService(
  private val referenceData: NomisReferenceDataProvider,
  private val monitoringService: MonitoringService
) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val locations: MutableMap<String, String> = mutableMapOf()

  fun findAgencyLocationNameBy(agencyId: String): String? =
    when (val location = locations[agencyId.trim().toUpperCase()]) {
      null -> {
        logger.warn("No match found looking up reference data for agency id '${agencyId.trim().toUpperCase()}'")
        monitoringService.capture("No match found looking up reference data for agency id '${agencyId.trim().toUpperCase()}'")
        null
      }
      else -> location
    }

  @PostConstruct
  internal fun loadInMemoryNomisLocationsReferenceData() {
    Result.runCatching {
      referenceData.get().reader().useLines { seq ->
        seq.filterIndexed { line, _ -> line != HEADER_ROW }
          .map { line -> line.split(",") }
          .associateByTo(locations, { it[NOMIS_AGENCY_ID].trim().toUpperCase() }, { it[NAME].trim().toUpperCase() })
      }
    }.onSuccess {
      logger.info("NOMIS locations reference data loaded ${locations.size} locations")
    }.onFailure {
      logger.error("An error occurred loading the NOMIS location reference data.", it)

      monitoringService.capture("An error occurred loading the NOMIS location reference data.")
    }
  }
}
