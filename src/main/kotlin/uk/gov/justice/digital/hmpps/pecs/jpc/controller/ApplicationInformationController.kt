package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.MediaType
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.SessionAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Period

private val logger = loggerFor<ApplicationInformationController>()

/**
 * Provides application level information/alerts to be displayed to (or inform) the end users something important
 *
 * An example of this would be a bulk price adjustment is taking place, which in itself can take several minutes.
 */
@RestController
@RequestMapping(name = "Supplier information", path = ["/app"], produces = [MediaType.APPLICATION_JSON_VALUE])
@SessionAttributes(DATE_ATTRIBUTE, SUPPLIER_ATTRIBUTE)
class ApplicationInformationController(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService,
  private val effectiveYear: EffectiveYear,
  private val reportsService: ImportReportsService,
  private val timeSource: TimeSource,
) {

  @GetMapping(path = ["/info"])
  fun info(@ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): ApplicationInformation {
    logger.info("getting info for $supplier")

    return ApplicationInformation(
      when {
        isPriceAdjustmentInProgressFor(supplier) ->
          "A bulk price adjustment is currently in progress. Any further price changes will be prevented until the adjustment is complete."
        isNotAbleToUpdatePricesForSelectedYear(model) ->
          "Prices for the selected catalogue year ${model.getSelectedYearStart().month()} ${model.getSelectedYearStart().year()
          } to ${model.getSelectedYearEnd().month()} ${model.getSelectedYearEnd().year()} can no longer be changed."
        isPreviousContractualYear(model) ->
          "Adjusting prices for the selected catalogue year ${model.getSelectedYearStart().month()} ${model.getSelectedYearStart().year()
          } to ${model.getSelectedYearEnd().month()} ${model.getSelectedYearEnd().year()}  will affect prices in the current year. " +
            "If inflationary adjustments have been made for the current year, please re-apply them."
        isPricingDataIsOutByTwoDays() -> "The service may be missing pricing data, please contact the Book a secure move team."
        else -> null
      },
    )
  }

  private fun isPriceAdjustmentInProgressFor(supplier: Supplier) =
    annualPriceAdjustmentsService.adjustmentInProgressFor(supplier)

  private fun isPreviousContractualYear(model: ModelMap) =
    effectiveYear.previous() == model.getSelectedEffectiveYear()

  private fun isNotAbleToUpdatePricesForSelectedYear(model: ModelMap) =
    !effectiveYear.canAddOrUpdatePrices(model.getSelectedEffectiveYear())

  private fun isPricingDataIsOutByTwoDays() =
    Period.between(reportsService.dateOfLastImport() ?: timeSource.date(), timeSource.date()).days > 1

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class ApplicationInformation(val message: String? = null)
}
