package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidDuplicateLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.DROP_OFF_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.PICK_UP_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.LocationsService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE, PICK_UP_ATTRIBUTE, DROP_OFF_ATTRIBUTE)
@ConditionalOnWebApplication
class MapFriendlyLocationController(
  private val service: LocationsService,
  private val basmClientApiService: BasmClientApiService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping("/map-location/{agency-id}")
  fun mapFriendlyLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): String {
    logger.info("getting map friendly location for $agencyId")

    val from = model.getAttribute(PICK_UP_ATTRIBUTE)
    val to = model.getAttribute(DROP_OFF_ATTRIBUTE)
    val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

    from.takeUnless { it == "" }.apply { url.queryParam(PICK_UP_ATTRIBUTE, from) }
    to.takeUnless { it == "" }.apply { url.queryParam(DROP_OFF_ATTRIBUTE, to) }

    model.addAttribute("cancelLink", url.build().toUriString())

    val nomisLocationName = basmClientApiService.findAgencyLocationNameBy(agencyId) ?: "Sorry, we are currently unable to retrieve the NOMIS Location Name. Please try again later."

    service.findAgencyLocationAndType(agencyId)?.let {
      model.addAttribute(
        "form",
        MapLocationForm(
          agencyId = it.first,
          locationName = it.second,
          locationType = it.third.name,
          operation = "update",
          nomisLocationName = nomisLocationName
        )
      )

      return "map-location"
    }

    model.addAttribute(
      "form",
      MapLocationForm(
        agencyId = agencyId,
        operation = "create",
        nomisLocationName = nomisLocationName
      )
    )

    return "map-location"
  }

  @PostMapping("/map-location")
  fun mapFriendlyLocation(@Valid @ModelAttribute("form") form: MapLocationForm, result: BindingResult, model: ModelMap, redirectAttributes: RedirectAttributes): String {
    logger.info("mapping friendly location for agency id ${form.agencyId}")

    val from = model.getAttribute(PICK_UP_ATTRIBUTE)
    val to = model.getAttribute(DROP_OFF_ATTRIBUTE)
    val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

    from.takeUnless { it == "" }.apply { url.queryParam(PICK_UP_ATTRIBUTE, from) }
    to.takeUnless { it == "" }.apply { url.queryParam(DROP_OFF_ATTRIBUTE, to) }

    model.addAttribute("cancelLink", url.build().toUriString())

    if (result.hasErrors()) {
      return "map-location"
    }

    service.mapFriendlyLocation(form.agencyId, form.locationName, LocationType.valueOf(form.locationType))

    redirectAttributes.addFlashAttribute("flashAttrMappedLocationName", form.locationName.toUpperCase())
    redirectAttributes.addFlashAttribute("flashAttrMappedAgencyId", form.agencyId)

    if (form.operation == "create") {
      redirectAttributes.addFlashAttribute("flashMessage", "location-mapped")
      return "redirect:/journeys"
    }

    if (form.operation == "update") {
      redirectAttributes.addFlashAttribute("flashMessage", "location-updated")
      return "redirect:${url.build().toUri()}"
    }

    throw Throwable("Invalid operation ${form.operation}")
  }

  @ValidDuplicateLocation
  data class MapLocationForm(
    @get: NotEmpty(message = "Enter NOMIS agency id")
    val agencyId: String,

    @get: NotEmpty(message = "Enter Schedule 34 location")
    val locationName: String = "",

    @get: NotEmpty(message = "Enter Schedule 34 location type")
    val locationType: String = "",

    val operation: String = "create",

    val nomisLocationName: String
  )
}
