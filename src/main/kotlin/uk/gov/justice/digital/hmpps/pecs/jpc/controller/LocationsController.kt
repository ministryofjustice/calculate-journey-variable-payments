package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.service.LocationsService

@RestController
@RequestMapping(name = "Locations", path = ["/locations"], produces = [MediaType.APPLICATION_JSON_VALUE])
class
LocationsController(@Autowired val locationService: LocationsService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping
  fun all(@RequestParam(name = "version") userVersion: Long): LocationsDto {
    logger.info("getting locations")

    val locationsVersion = locationService.getVersion()

    return LocationsDto(
      locationsVersion,
      if (userVersion != locationsVersion) locationService.findAll().associateBy({ it.nomisAgencyId }, { it.siteName })
      else null
    )
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class LocationsDto(val version: Long, val locations: Map<String, String>? = null)
}
