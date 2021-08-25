package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Controller to handle the flows around 'Annual Price Adjustments' carried out by the commercial team.
 */
@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class AnnualPriceAdjustmentsController {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun index(model: ModelMap): Any {
    logger.info("getting annual price adjustment")

    model.apply {
      addContractStartAndEndDates()
      addAttribute("form", AnnualPriceAdjustmentForm("0.000"))
    }

    return "annual-price-adjustment"
  }

  @PostMapping(APPLY_ANNUAL_PRICE_ADJUSTMENT)
  fun applyAnnualPriceAdjustment(
    @Valid @ModelAttribute("form") form: AnnualPriceAdjustmentForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("posting annual price adjustment")

    TODO()
  }

  data class AnnualPriceAdjustmentForm(
    @get: NotNull(message = "Apply an annual price adjustment rate")
    val rate: String,
  )

  companion object {
    const val ANNUAL_PRICE_ADJUSTMENT = "/annual-price-adjustment"
    const val APPLY_ANNUAL_PRICE_ADJUSTMENT = "/apply-annual-price-adjustment"

    fun routes(): Array<String> = arrayOf(ANNUAL_PRICE_ADJUSTMENT, APPLY_ANNUAL_PRICE_ADJUSTMENT)
  }
}
