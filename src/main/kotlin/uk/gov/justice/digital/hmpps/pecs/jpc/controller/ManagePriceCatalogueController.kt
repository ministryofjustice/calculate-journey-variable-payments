package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.SessionAttributes

/**
 * Controller for managing page flows around pricing of journeys. This is accessed from the main menu link 'Manage Price Catalogue'.
 */
@Controller
@SessionAttributes(
  DATE_ATTRIBUTE
)
class ManagePriceCatalogueController {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(SEARCH_JOURNEYS_URL)
  fun searchJourneys(model: ModelMap): Any {
    logger.info("getting search journey")

    val effectiveYear = model.getEffectiveYear()

    model.addAttribute("form", HtmlController.SearchJourneyForm())
    model.addAttribute("contractualYearStart", "$effectiveYear")
    model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")
    return "search-journeys"
  }

  companion object {
    const val SEARCH_JOURNEYS_URL = "/search-journeys"

    fun routes(): Array<String> =
      arrayOf(SEARCH_JOURNEYS_URL)
  }
}
