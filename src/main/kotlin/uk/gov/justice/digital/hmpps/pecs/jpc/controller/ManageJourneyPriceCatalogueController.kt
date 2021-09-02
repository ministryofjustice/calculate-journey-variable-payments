package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints.ValidJourneySearch
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import javax.validation.Valid

/**
 * Controller to manage flows around journey pricing. This is accessed from the main menu link 'Manage Journey Price Catalogue'.
 */
@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE, PICK_UP_ATTRIBUTE, DROP_OFF_ATTRIBUTE)
class ManageJourneyPriceCatalogueController(@Autowired val journeyService: JourneyService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(MANAGE_JOURNEY_PRICE_CATALOGUE)
  fun index(model: ModelMap): Any {
    model.addContractStartAndEndDates()

    return "manage-journey-price-catalogue"
  }

  @GetMapping(SEARCH_JOURNEYS_URL)
  fun searchJourneys(model: ModelMap): Any {
    logger.info("getting search journey")

    model.addAttribute("form", SearchJourneyForm())
    model.addContractStartAndEndDates()
    return "search-journeys"
  }

  @GetMapping(SEARCH_JOURNEYS_RESULTS_URL)
  fun searchJourneys(
    @RequestParam(name = PICK_UP_ATTRIBUTE, required = false) pickUpLocation: String?,
    @RequestParam(name = DROP_OFF_ATTRIBUTE, required = false) dropOffLocation: String?,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap
  ): Any {
    logger.info("getting search journey results for $supplier")

    if (pickUpLocation.isNullOrEmpty() && dropOffLocation.isNullOrEmpty()) {
      return RedirectView(SEARCH_JOURNEYS_URL)
    }

    val effectiveYear = model.getSelectedEffectiveYear()
    val journeys = journeyService.prices(supplier, pickUpLocation, dropOffLocation, effectiveYear)

    model.addAttribute("contractualYearStart", "$effectiveYear")
    model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")

    return if (journeys.isEmpty()) {
      model.addAttribute("pickUpLocation", pickUpLocation ?: "")
      model.addAttribute("dropOffLocation", dropOffLocation ?: "")
      "no-search-journeys-results"
    } else {
      model.addAttribute("journeys", journeys)
      "search-journeys-results"
    }
  }

  @PostMapping(SEARCH_JOURNEYS_URL)
  fun performJourneySearch(
    @Valid @ModelAttribute("form") form: SearchJourneyForm,
    result: BindingResult,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap,
    redirectAttributes: RedirectAttributes,
  ): String {
    logger.info("performing search journeys for $supplier")

    if (result.hasErrors()) return "search-journeys"

    model.removeAnyPreviousSearchHistory()

    val from = form.from?.trim().orEmpty()
    val to = form.to?.trim().orEmpty()

    val url = UriComponentsBuilder.fromUriString(SEARCH_JOURNEYS_RESULTS_URL)

    if (from.isNotBlank()) {
      url.queryParam(PICK_UP_ATTRIBUTE, from)
      model.addAttribute(PICK_UP_ATTRIBUTE, from)
    }

    if (to.isNotBlank()) {
      url.queryParam(DROP_OFF_ATTRIBUTE, to)
      model.addAttribute(DROP_OFF_ATTRIBUTE, to)
    }

    return "redirect:${url.build().toUri()}"
  }

  @ValidJourneySearch
  data class SearchJourneyForm(val from: String? = null, val to: String? = null)

  companion object {
    const val MANAGE_JOURNEY_PRICE_CATALOGUE = "/manage-journey-price-catalogue"
    const val SEARCH_JOURNEYS_URL = "/search-journeys"
    const val SEARCH_JOURNEYS_RESULTS_URL = "/journeys-results"

    fun routes(): Array<String> =
      arrayOf(MANAGE_JOURNEY_PRICE_CATALOGUE, SEARCH_JOURNEYS_URL, SEARCH_JOURNEYS_RESULTS_URL)
  }
}
