package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.SessionAttributes

/**
 * Controller to handle the flows around 'Annual Price Adjustments' carried out by the commercial team.
 */
@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class AnnualPriceAdjustmentsController {

  @GetMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun index(model: ModelMap): Any {
    model.addContractStartAndEndDates()

    return "annual-price-adjustment"
  }

  companion object {
    const val ANNUAL_PRICE_ADJUSTMENT = "/annual-price-adjustment"

    fun routes(): Array<String> = arrayOf(ANNUAL_PRICE_ADJUSTMENT)
  }
}
