package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.beust.klaxon.Klaxon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService

@RestController
class LocationsJSONController(@Autowired val locationService: MapFriendlyLocationService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(ALL_URL)
  @ResponseBody
  fun all(@ModelAttribute(name = LOCATIONS_VERSION_ATTRIBUTE) userVersion: Long, model: ModelMap): Any {
    logger.info("getting locations json")

    val locationsVersion = locationService.getVersion()
    val locationsData: HashMap<String, Any> = hashMapOf("version" to locationsVersion)

    if (userVersion != locationsVersion) {
      val locationMap = hashMapOf<String, String>()
      locationService.findAll().forEach { locationMap[it.nomisAgencyId] = it.siteName }
      locationsData["locations"] = locationMap
    }

    return Klaxon().toJsonString(locationsData)
  }

  companion object {
    const val LOCATIONS_VERSION_ATTRIBUTE = "locationsVersion"
    const val ALL_URL = "/locations.json"
  }
}
