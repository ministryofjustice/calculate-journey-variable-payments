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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import javax.validation.Valid
import javax.validation.constraints.Pattern

/**
 * Controller to handle the flows around 'Annual Price Adjustments' carried out by the commercial team.
 */
@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class AnnualPriceAdjustmentsController(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService,
  private val effectiveYear: EffectiveYear
) {

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

  @PostMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun applyAnnualPriceAdjustment(
    @Valid @ModelAttribute("form") form: AnnualPriceAdjustmentForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("posting annual price adjustment")

    val mayBeRate = form.mayBeRate() ?: result.rejectInvalidAdjustmentRate().let { null }

    if (result.hasErrors() || mayBeRate == null) {
      model.addContractStartAndEndDates()

      return "annual-price-adjustment"
    }

    annualPriceAdjustmentsService.adjust(supplier, effectiveYear.current(), mayBeRate)

    return "manage-journey-price-catalogue"
  }

  private fun BindingResult.rejectInvalidAdjustmentRate() {
    this.rejectValue("rate", "rate", "Invalid rate")
  }

  data class AnnualPriceAdjustmentForm(
    @get: Pattern(regexp = "^[0-9]{1,5}(\\.[0-9]{0,3})?\$", message = "Invalid rate")
    val rate: String?,
  ) {
    fun mayBeRate() = rate?.toDoubleOrNull()?.takeIf { it > 0 }
  }

  companion object {
    const val ANNUAL_PRICE_ADJUSTMENT = "/annual-price-adjustment"

    fun routes(): Array<String> = arrayOf(ANNUAL_PRICE_ADJUSTMENT)
  }
}
