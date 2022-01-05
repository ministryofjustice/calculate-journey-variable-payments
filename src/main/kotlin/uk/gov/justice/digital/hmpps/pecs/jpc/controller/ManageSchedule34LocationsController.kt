package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.hibernate.validator.constraints.Length
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.LocationsService
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Controller to support searching and maintenance of existing Schedule 34 locations.
 */
@SessionAttributes(SUPPLIER_ATTRIBUTE)
@Controller
class ManageSchedule34LocationsController(
  private val service: LocationsService,
  private val basmClientApiService: BasmClientApiService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ModelAttribute("navigation")
  fun navigation() = "LOCATION"

  @GetMapping(SEARCH_LOCATIONS)
  fun showSearchLocation(model: ModelMap): Any {
    logger.info("showing location search")

    model.addAttribute("form", SearchLocationForm())

    return "search-locations"
  }

  @PostMapping(SEARCH_LOCATIONS)
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
      else -> "redirect:$MANAGE_LOCATION/${location.nomisAgencyId}"
    }
  }

  private fun BindingResult.locationNotfound(location: String) {
    this.rejectValue("location", "notfound", "Location ${location.trim().uppercase()} not found")
  }

  @GetMapping("$MANAGE_LOCATION/{agency-id}")
  fun showManageLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): Any {
    logger.info("showing manage location")

    service.findAgencyLocationAndType(agencyId).let {
      if (it == null) throw ResourceNotFoundException("Location with agency id $agencyId not found.")

      model.addAttribute(
        "form",
        LocationForm(
          agencyId = it.first,
          locationName = it.second,
          locationType = it.third,
          nomisLocationName = basmClientApiService.findNomisAgencyLocationNameBy(agencyId)
            ?: "Sorry, we are currently unable to retrieve the NOMIS Location Name. Please try again later."
        )
      )

      model.addAttribute(
        "history",
        service.locationHistoryForAgencyId(agencyId)
          .map { history -> LocationHistoryDto.valueOf(history) }
          .sortedByDescending { lh -> lh.datetime }
      )
    }

    return "manage-location"
  }

  @PostMapping(MANAGE_LOCATION)
  fun performManageLocation(
    @Valid @ModelAttribute("form") form: LocationForm,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes
  ): String {
    logger.info("performing manage location")

    result.rejectIfContainsInvalidCharacters(form.locationName, "locationName", "Invalid location")

    if (result.hasErrors()) {
      model.addAttribute(
        "history",
        service.locationHistoryForAgencyId(form.agencyId)
          .map { history -> LocationHistoryDto.valueOf(history) }
          .sortedByDescending { lh -> lh.datetime }
      )

      return "manage-location"
    }

    return if (isDuplicate(form)) {
      "manage-location".also { result.duplicateLocation() }
    } else {
      service.setLocationDetails(form.agencyId, form.locationName, form.locationType!!)

      redirectAttributes.addFlashAttribute("flashMessage", "location-updated")
      redirectAttributes.addFlashAttribute("flashAttrMappedLocationName", form.locationName.uppercase())
      redirectAttributes.addFlashAttribute("flashAttrMappedAgencyId", form.agencyId)

      return "redirect:search-locations"
    }
  }

  private fun isDuplicate(form: LocationForm) = service.locationAlreadyExists(form.agencyId, form.locationName)

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
    @get: Length(max = 255, message = "Enter details upto 255 characters")
    val locationName: String = "",

    @get: NotNull(message = "Please enter a schedule 34 location type")
    val locationType: LocationType? = null,

    val nomisLocationName: String = ""
  )

  companion object Routes {

    const val MANAGE_LOCATION = "/manage-location"
    const val SEARCH_LOCATIONS = "/search-locations"

    fun routes(): Array<String> = arrayOf(MANAGE_LOCATION, "$MANAGE_LOCATION/*", SEARCH_LOCATIONS)
  }
}
