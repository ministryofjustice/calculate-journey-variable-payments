package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.hibernate.validator.constraints.Length
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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

/**
 * Controller to handle the flows around 'Annual Price Adjustments' carried out by the commercial team.
 */
private val logger = loggerFor<AnnualPriceAdjustmentsController>()

@Controller
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class AnnualPriceAdjustmentsController(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService,
  private val actualEffectiveYear: EffectiveYear
) {

  @ModelAttribute("navigation")
  fun navigation() = "PRICE"

  @GetMapping(ANNUAL_PRICE_ADJUSTMENT)
  fun index(model: ModelMap, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {
    logger.info("getting annual price adjustment")

    model.addContractStartAndEndDates()
    model.addAdjustmentHistoryFor(supplier)

    if (model.getSelectedEffectiveYear().isBefore(actualEffectiveYear)) {
      return "annual-price-adjustment-history"
    }

    model.apply {
      addAttribute("form", AnnualPriceAdjustmentForm("0.0", "0.0"))
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

    val mayBeInflationaryRate = form.mayBeInflationaryRate() ?: result.rejectInvalidAdjustmentRate("inflationaryRate").let { null }
    val mayBeVolumetricRate = form.mayBeVolumetricRate() ?: result.rejectInvalidAdjustmentRate("volumetricRate").let { null }

    form.details?.let { result.rejectIfContainsInvalidCharacters(it, "details", "Invalid details") }

    if (result.hasErrors() || form.details == null || mayBeInflationaryRate == null || mayBeVolumetricRate == null) {
      model.apply {
        addContractStartAndEndDates()
        addAdjustmentHistoryFor(supplier)
      }

      return "annual-price-adjustment"
    }

    annualPriceAdjustmentsService.adjust(
      supplier = supplier,
      suppliedEffective = model.getSelectedEffectiveYear(),
      inflationary = mayBeInflationaryRate,
      volumetric = mayBeVolumetricRate.takeIfNotNullOrZero(),
      authentication = authentication,
      details = form.details
    )

    return "redirect:/manage-journey-price-catalogue"
  }

  private fun AdjustmentMultiplier.takeIfNotNullOrZero() = this.takeIf { it.value < BigDecimal.ZERO || it.value > BigDecimal.ZERO }

  private fun BindingResult.rejectInvalidAdjustmentRate(field: String) {
    this.rejectValue(field, "rate", "Invalid rate")
  }

  private fun priceAdjustmentHistoryFor(supplier: Supplier): List<PriceAdjustmentHistoryDto> =
    annualPriceAdjustmentsService.adjustmentsHistoryFor(supplier)
      .map { history -> PriceAdjustmentHistoryDto.valueOf(supplier, history) }
      .sortedByDescending { lh -> lh.datetime }

  private fun ModelMap.addAdjustmentHistoryFor(supplier: Supplier) {
    this.addAttribute("history", priceAdjustmentHistoryFor(supplier))
  }

  data class AnnualPriceAdjustmentForm(
    @get: Pattern(regexp = "^[0-9]{0,2}(\\.[0-9]{0,40})?\$", message = "Invalid rate")
    val inflationaryRate: String?,

    @get: Pattern(regexp = "^-?[0-9]{0,2}(\\.[0-9]{0,40})?\$", message = "Invalid volumetric rate")
    val volumetricRate: String?,

    @get: NotBlank(message = "Enter details upto 255 characters")
    @get: Length(max = 255, message = "Enter details upto 255 characters")
    val details: String? = null
  ) {
    fun mayBeInflationaryRate() =
      inflationaryRate?.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }?.let { AdjustmentMultiplier(it) }

    fun mayBeVolumetricRate() =
      volumetricRate?.toBigDecimalOrNull()?.let { AdjustmentMultiplier(it) }
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
        "Prices adjusted by rate of ${data.multiplier}",
        if (AuditableEvent.isSystemGenerated(event)) "SYSTEM" else event.username,
        data.details
      )
    }
  }
}
