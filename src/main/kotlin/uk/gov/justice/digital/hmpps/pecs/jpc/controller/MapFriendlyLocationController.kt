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
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE, "operation")
class MapFriendlyLocationController(private val service: MapFriendlyLocationService) {

  @GetMapping("/map-location/{agency-id}")
  fun mapFriendlyLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): String {

    service.findAgencyLocationAndType(agencyId)?.let {
      model.addAttribute("operation", "update")
      model.addAttribute("form", MapLocationForm(agencyId = it.first, locationName = it.second, locationType = it.third.name))

      return "map-location"
    }

    model.addAttribute("operation", "create")
    model.addAttribute("form", MapLocationForm(agencyId = agencyId))

    return "map-location"
  }

  @PostMapping("/map-location")
  fun mapFriendlyLocation(@Valid @ModelAttribute("form") form: MapLocationForm, result: BindingResult, model: ModelMap, redirectAttributes: RedirectAttributes): Any {
    if (result.hasErrors()) {
      return "/map-location"
    }

    if (service.locationAlreadyExists(form.agencyId, form.locationName)) {
      // TODO need to display error

      return "/map-location"
    }

    service.mapFriendlyLocation(form.agencyId, form.locationName, LocationType.valueOf(form.locationType))

    redirectAttributes.addFlashAttribute("mappedLocationName", form.locationName)
    redirectAttributes.addFlashAttribute("mappedAgencyId", form.agencyId)

    return RedirectView("/journeys", true)
  }

  data class MapLocationForm(
          @get: NotEmpty(message = "Enter NOMIS agency id")
          val agencyId: String,

          @get: NotEmpty(message = "Enter Schedule 34 location")
          val locationName: String = "",

          @get: NotEmpty(message = "Enter Schedule 34 location type")
          val locationType: String = ""
  )
}

