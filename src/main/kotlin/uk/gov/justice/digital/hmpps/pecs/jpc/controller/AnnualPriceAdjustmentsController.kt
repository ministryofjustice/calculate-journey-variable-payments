package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.hibernate.validator.constraints.Length
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AnnualPriceAdjustmentMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

/**
 * Controller to handle the flows around 'Annual Price Adjustments' carried out by the commercial team.
 */
@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class AnnualPriceAdjustmentsController(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService,
  private val actualEffectiveYear: EffectiveYear
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun index(model: ModelMap, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {
    logger.info("getting annual price adjustment")

    if (model.getEffectiveYear().isBefore(actualEffectiveYear)) {
      model.addContractStartAndEndDates()
      model.addAttribute("history", priceAdjustmentHistoryFor(supplier))

      return "annual-price-adjustment-history"
    }

    model.apply {
      addContractStartAndEndDates()
      addAttribute("form", AnnualPriceAdjustmentForm("0.0000"))
      addAttribute("history", priceAdjustmentHistoryFor(supplier))
    }

    return "annual-price-adjustment"
  }

  private fun Int.isBefore(effectiveYear: EffectiveYear): Boolean = this < effectiveYear.current()

  @PostMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun applyAnnualPriceAdjustment(
    @Valid @ModelAttribute("form") form: AnnualPriceAdjustmentForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
    authentication: Authentication?
  ): Any {
    logger.info("posting annual price adjustment")

    val mayBeRate = form.mayBeRate() ?: result.rejectInvalidAdjustmentRate().let { null }

    if (result.hasErrors() || mayBeRate == null) {
      model.apply {
        addContractStartAndEndDates()
        addAttribute("history", priceAdjustmentHistoryFor(supplier))
      }

      return "annual-price-adjustment"
    }

    annualPriceAdjustmentsService.adjust(supplier, model.getEffectiveYear(), mayBeRate, authentication, form.details!!)

    return "manage-journey-price-catalogue"
  }

  private fun BindingResult.rejectInvalidAdjustmentRate() {
    this.rejectValue("rate", "rate", "Invalid rate")
  }

  private fun priceAdjustmentHistoryFor(supplier: Supplier): List<PriceAdjustmentHistoryDto> =
    annualPriceAdjustmentsService.adjustmentsHistoryFor(supplier)
      .map { history -> PriceAdjustmentHistoryDto.valueOf(supplier, history) }
      .sortedByDescending { lh -> lh.datetime }

  data class AnnualPriceAdjustmentForm(
    @get: Pattern(regexp = "^[0-9]{1,5}(\\.[0-9]{0,4})?\$", message = "Invalid rate")
    val rate: String?,

    @get: NotEmpty(message = "Enter details upto 255 characters")
    @get: Length(max = 255, message = "Enter details upto 255 characters")
    val details: String? = null
  ) {
    fun mayBeRate() = rate?.toDoubleOrNull()?.takeIf { it > 0 }
  }

  companion object {
    const val ANNUAL_PRICE_ADJUSTMENT = "/annual-price-adjustment"

    fun routes(): Array<String> = arrayOf(ANNUAL_PRICE_ADJUSTMENT)
  }
}

data class PriceAdjustmentHistoryDto(
  val datetime: LocalDateTime,
  val action: String,
  val by: String,
  val details: String
) {
  companion object {
    /**
     * Throws a runtime exception if the [AuditEvent] is not a journey price event or if there is a supplier mismatch.
     */
    fun valueOf(supplier: Supplier, event: AuditEvent): PriceAdjustmentHistoryDto {
      val data = AnnualPriceAdjustmentMetadata.map(event)

      if (data.supplier != supplier) throw RuntimeException("Audit bulk price adjusted event not for supplier $supplier")

      return PriceAdjustmentHistoryDto(
        event.createdAt,
        "Prices adjusted by blended rate of ${data.multiplier}",
        if (AuditableEvent.isSystemGenerated(event)) "SYSTEM" else event.username,
        data.details
      )
    }
  }
}
