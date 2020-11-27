package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE)
class MapFriendlyLocationController(private val service: MapFriendlyLocationService) {

  @GetMapping("/map-location/{agency-id}")
  fun mapFriendlyLocation(@PathVariable("agency-id") agencyId: String, model: ModelMap): String {

    model.addAttribute("form", MapLocationForm(agencyId = agencyId))

    return "map-location"
  }

  @PostMapping("/map-location")
  fun mapFriendlyLocation(@Valid @ModelAttribute("form") form: MapLocationForm, result: BindingResult, model: ModelMap): Any {
    if (result.hasErrors()) {
      return "/map-location"
    }

    service.mapFriendlyLocation(form.agencyId, form.locationName, LocationType.valueOf(form.locationType))

    // TODO this needs to go back to the journeys for review page when it is ready/available!
    return RedirectView(HtmlController.DASHBOARD_URL)
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
