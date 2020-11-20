package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.SessionAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyMapLocationService

@Controller
@SessionAttributes("supplier")
class JourneyMapLocationController(private val service: JourneyMapLocationService) {

  @GetMapping("/map-pickup-location/{journey-id}")
  fun mapPickupLocation(@PathVariable("journey-id") journeyId: String,
                        @ModelAttribute(name = "supplier") supplier: Supplier,
                        model: ModelMap): String {

    val agencies = service.findPickUpAndDropOffAgenciesForJourney(journeyId)

    // TODO check pair present if not then matching journey not found

    model.addAttribute("form", agencies?.first?.let { MapLocationForm(agencyId = it) })

    return "map-location"
  }

  @GetMapping("/map-drop-off-location/{journey-id}")
  fun mapDropOffLocation(@PathVariable("journey-id") journeyId: String,
                         @ModelAttribute(name = "supplier") supplier: Supplier,
                         model: ModelMap): String {

    val agencies = service.findPickUpAndDropOffAgenciesForJourney(journeyId)

    // TODO check pair present and from agency found then not found

    model.addAttribute("form", agencies?.second?.let { MapLocationForm(agencyId = it) })

    return "map-location"
  }

  data class MapLocationForm(val agencyId: String, val locationName: String = "", val locationType: String = "")
}
