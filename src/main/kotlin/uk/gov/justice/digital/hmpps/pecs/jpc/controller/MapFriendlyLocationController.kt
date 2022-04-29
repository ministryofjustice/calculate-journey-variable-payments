package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.hibernate.validator.constraints.Length
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageJourneyPriceCatalogueController.Companion.SEARCH_JOURNEYS_RESULTS_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController.Companion.LOCATION_ORIGIN_SESSION_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints.ValidDuplicateLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.LocationsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Controller to help with mapping a user-friendly location name to agency ID Schedule 34 locations names.
 */
private val logger = loggerFor<MapFriendlyLocationController>()

@Controller
@SessionAttributes(SUPPLIER_ATTRIBUTE, PICK_UP_ATTRIBUTE, DROP_OFF_ATTRIBUTE, LOCATION_ORIGIN_SESSION_ATTRIBUTE)
@ConditionalOnWebApplication
class MapFriendlyLocationController(
  private val service: LocationsService,
  private val basmClientApiService: BasmClientApiService
) : PrimaryNavigationBar {

  override fun primaryNavigationChoice() = PrimaryNavigation.LOCATION

  @GetMapping("$MAP_LOCATION/{agency-id}")
  fun mapFriendlyLocation(
    @PathVariable("agency-id") agencyId: String,
    @RequestParam(name = LOCATION_ORIGIN_SESSION_ATTRIBUTE, required = false) origin: String?,
    model: ModelMap
  ): String {
    logger.info("getting map friendly location for $agencyId")

    val nomisLocationName = basmClientApiService.findNomisAgencyLocationNameBy(agencyId)
      ?: "Sorry, we are currently unable to retrieve the NOMIS Location Name. Please try again later."

    service.findAgencyLocationAndType(agencyId)?.let {
      model.addAttribute(
        "form",
        MapLocationForm(
          agencyId = it.first,
          locationName = it.second,
          locationType = it.third,
          operation = "update",
          nomisLocationName = nomisLocationName
        )
      )

      model.addAttribute(
        "history",
        service.locationHistoryForAgencyId(agencyId)
          .map { history -> LocationHistoryDto.valueOf(history) }
          .sortedByDescending { lh -> lh.datetime }
      )

      model.addCancelLinkFor(UriComponentsBuilder.fromUriString(SEARCH_JOURNEYS_RESULTS_URL))
      model.rememberLocationChangeTypePriorToUpdate(origin!!)

      return "update-location"
    }

    model.addAttribute(
      "form",
      MapLocationForm(
        agencyId = agencyId,
        operation = "create",
        nomisLocationName = nomisLocationName
      )
    )

    return "add-location"
  }

  private fun ModelMap.rememberLocationChangeTypePriorToUpdate(type: String) {
    this.addAttribute(LOCATION_ORIGIN_SESSION_ATTRIBUTE, type)
  }

  @PostMapping(MAP_LOCATION)
  fun mapFriendlyLocation(
    @Valid @ModelAttribute("form") form: MapLocationForm,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes
  ): String {
    logger.info("mapping friendly location for agency id ${form.agencyId}")

    val searchResultsUrl = UriComponentsBuilder.fromUriString(SEARCH_JOURNEYS_RESULTS_URL)

    result.rejectIfContainsInvalidCharacters(form.locationName, "locationName", "Invalid location")

    if (result.hasErrors()) {
      if (form.operation == "update") {
        model.addCancelLinkFor(searchResultsUrl)

        model.addAttribute(
          "history",
          service.locationHistoryForAgencyId(form.agencyId)
            .map { history -> LocationHistoryDto.valueOf(history) }
            .sortedByDescending { lh -> lh.datetime }
        )

        return "update-location"
      }

      return "add-location"
    }

    service.setLocationDetails(form.agencyId, form.locationName, form.locationType!!)

    if (form.operation == "create") {
      redirectAttributes.informUserOfChangesOnRedirect("location-mapped", form)
      return "redirect:/journeys"
    }

    if (form.operation == "update") {
      when (val origin = model.getLocationOrigin()) {
        "from" -> {
          reflectChangeToLocationWhenReturnToSearchResults {
            model.getOptionalAttribute(PICK_UP_ATTRIBUTE) {
              searchResultsUrl.queryParam(PICK_UP_ATTRIBUTE, form.locationName.uppercase())
              model.addAttribute(PICK_UP_ATTRIBUTE, form.locationName.uppercase())
            }
            model.getOptionalAttribute(DROP_OFF_ATTRIBUTE) { searchResultsUrl.queryParam(DROP_OFF_ATTRIBUTE, it) }
          }
        }
        "to" -> {
          reflectChangeToLocationWhenReturnToSearchResults {
            model.getOptionalAttribute(PICK_UP_ATTRIBUTE) { searchResultsUrl.queryParam(PICK_UP_ATTRIBUTE, it) }
            model.getOptionalAttribute(DROP_OFF_ATTRIBUTE) {
              searchResultsUrl.queryParam(DROP_OFF_ATTRIBUTE, form.locationName.uppercase())
              model.addAttribute(DROP_OFF_ATTRIBUTE, form.locationName.uppercase())
            }
          }
        }
        else -> throw RuntimeException("Unknown location origin '$origin'")
      }

      redirectAttributes.informUserOfChangesOnRedirect("location-updated", form)
      model.addAttribute(LOCATION_ORIGIN_SESSION_ATTRIBUTE, "")

      return "redirect:${searchResultsUrl.build().toUri()}"
    }

    throw Throwable("Invalid operation ${form.operation}")
  }

  private fun reflectChangeToLocationWhenReturnToSearchResults(callback: () -> Unit) = callback()

  private fun RedirectAttributes.informUserOfChangesOnRedirect(operation: String, form: MapLocationForm) {
    this.addFlashAttribute("flashMessage", operation)
    this.addFlashAttribute("flashAttrMappedLocationName", form.locationName.uppercase())
    this.addFlashAttribute("flashAttrMappedAgencyId", form.agencyId)
  }

  private fun ModelMap.addCancelLinkFor(url: UriComponentsBuilder) {
    this.getOptionalAttribute(PICK_UP_ATTRIBUTE) { pickUp -> url.queryParam(PICK_UP_ATTRIBUTE, pickUp) }
    this.getOptionalAttribute(DROP_OFF_ATTRIBUTE) { dropOff -> url.queryParam(DROP_OFF_ATTRIBUTE, dropOff) }
    this.addAttribute("cancelLink", url.build().toUriString())
  }

  private fun ModelMap.getOptionalAttribute(attr: String, callback: (s: String) -> Unit) {
    when (val value = this.getAttribute(attr)) {
      is String -> if (value.isNotBlank()) callback(value)
    }
  }

  private fun ModelMap.getLocationOrigin(): String {
    when (val value = this.getAttribute(LOCATION_ORIGIN_SESSION_ATTRIBUTE)) {
      is String -> if (value.isNotBlank()) return value else throw RuntimeException("Expected attribute '$LOCATION_ORIGIN_SESSION_ATTRIBUTE' is null or empty")
      else -> throw NullPointerException("Expected attribute '$LOCATION_ORIGIN_SESSION_ATTRIBUTE is missing")
    }
  }

  @ValidDuplicateLocation
  data class MapLocationForm(
    @get: NotEmpty(message = "Enter NOMIS agency id")
    val agencyId: String,

    @get: NotBlank(message = "Enter Schedule 34 location")
    @get: Length(max = 255, message = "Enter details upto 255 characters")
    val locationName: String = "",

    @get: NotNull(message = "Enter Schedule 34 location type")
    val locationType: LocationType? = null,

    val operation: String = "create",

    val nomisLocationName: String
  )

  companion object {
    const val LOCATION_ORIGIN_SESSION_ATTRIBUTE = "origin"

    const val MAP_LOCATION = "/map-location"
  }
}
