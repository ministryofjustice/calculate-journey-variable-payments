package uk.gov.justice.digital.hmpps.pecs.jpc.controller

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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE, HtmlController.PICK_UP_ATTRIBUTE, HtmlController.DROP_OFF_ATTRIBUTE)
class MapFriendlyLocationController(private val service: MapFriendlyLocationService) {

    @GetMapping("/map-location/{agency-id}")
    fun mapFriendlyLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): String {
        val from = model.getAttribute(HtmlController.PICK_UP_ATTRIBUTE)
        val to = model.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE)
        val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

        from.takeUnless { it == "" }.apply { url.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from) }
        to.takeUnless { it == "" }.apply { url.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to) }

        model.addAttribute("cancelLink", url.build().toUriString())

        service.findAgencyLocationAndType(agencyId)?.let {
            model.addAttribute("form", MapLocationForm(agencyId = it.first, locationName = it.second, locationType = it.third.name, operation = "update"))

            return "map-location"
        }

        model.addAttribute("form", MapLocationForm(agencyId = agencyId, operation = "create"))

        return "map-location"
    }

    @PostMapping("/map-location")
    fun mapFriendlyLocation(@Valid @ModelAttribute("form") form: MapLocationForm, result: BindingResult, model: ModelMap, redirectAttributes: RedirectAttributes): String {
        if (result.hasErrors()) {
            return "/map-location"
        }

        service.mapFriendlyLocation(form.agencyId, form.locationName, LocationType.valueOf(form.locationType))

        redirectAttributes.addFlashAttribute("flashAttrMappedLocationName", form.locationName)
        redirectAttributes.addFlashAttribute("flashAttrMappedAgencyId", form.agencyId)

        if (form.operation == "create") {
            redirectAttributes.addFlashAttribute("flashMessage", "location-mapped")
            return "redirect:/journeys"
        }

        if (form.operation == "update") {
            val from = model.getAttribute(PICK_UP_ATTRIBUTE)
            val to = model.getAttribute(DROP_OFF_ATTRIBUTE)
            val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)
            if (from != "") {
                url.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from)
            }
            if (to != "") {
                url.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to)
            }
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
    )
}

