package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.LocationsService
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Controller
class ManageLocationsController(
  private val service: LocationsService,
  private val basmClientApiService: BasmClientApiService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping("/search-locations")
  fun showSearchLocation(model: ModelMap): Any {
    logger.info("getting map location search")
    model.addAttribute("form", SearchLocationForm())

    return "search-locations"
  }

  @PostMapping("/search-locations")
  fun searchForLocation(
    @Valid @ModelAttribute("form") form: SearchLocationForm,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes
  ): Any {
    logger.info("searching for location")

    if (result.hasErrors()) return "search-locations"

    return when (val location = service.findLocationBySiteName(form.location!!)) {
      null -> return "search-locations".also { result.locationNotfound(form.location) }
      else -> "redirect:/manage-location/${location.nomisAgencyId}"
    }
  }

  private fun BindingResult.locationNotfound(location: String) {
    this.rejectValue("location", "notfound", "Location ${location.trim().toUpperCase()} not found")
  }

  @GetMapping("/manage-location/{agency-id}")
  fun showManageLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): Any {
    logger.info("getting manage location")

    service.findAgencyLocationAndType(agencyId).let {
      if (it == null) throw ResourceNotFoundException("Location with agency id $agencyId not found.")

      model.addAttribute(
        "form",
        LocationForm(
          agencyId = it.first,
          locationName = it.second,
          locationType = it.third.name,
          nomisLocationName = basmClientApiService.findAgencyLocationNameBy(agencyId)
            ?: "Sorry, we are currently unable to retrieve the NOMIS Location Name. Please try again later."
        )
      )
    }

    return "manage-location"
  }

  @PostMapping("/manage-location")
  fun performManageLocation(
    @Valid @ModelAttribute("form") location: LocationForm,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes
  ): String {
    if (result.hasErrors()) return "manage-location"

    return if (duplicate(location)) {
      "manage-location".also { result.duplicateLocation() }
    } else {
      service.mapFriendlyLocation(location.agencyId, location.locationName, LocationType.valueOf(location.locationType))

      redirectAttributes.addFlashAttribute("flashMessage", "location-updated")
      redirectAttributes.addFlashAttribute("flashAttrMappedLocationName", location.locationName.toUpperCase())
      redirectAttributes.addFlashAttribute("flashAttrMappedAgencyId", location.agencyId)

      return "redirect:/search-locations"
    }
  }

  private fun duplicate(form: LocationForm) = service.locationAlreadyExists(form.agencyId, form.locationName)

  private fun BindingResult.duplicateLocation() {
    this.rejectValue("locationName", "duplicate", "There is a problem, Schedule 34 location entered already exists, please enter a new schedule 34 location")
  }

  data class SearchLocationForm(
    @get: NotBlank(message = "Please enter a schedule 34 location name")
    val location: String? = null
  )

  data class LocationForm(
    @get: NotBlank(message = "NOMIS agency id is required")
    val agencyId: String,

    @get: NotBlank(message = "Please enter a schedule 34 location name")
    val locationName: String = "",

    @get: NotBlank(message = "Please enter a schedule 34 location type")
    val locationType: String = "",

    val nomisLocationName: String
  )
}
