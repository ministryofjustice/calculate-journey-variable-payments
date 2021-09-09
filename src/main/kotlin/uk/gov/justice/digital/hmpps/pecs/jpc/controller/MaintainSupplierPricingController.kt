package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * Controller to handle requests for Supplier price maintenance. This includes adding new prices, updating existing
 * prices and displaying the history of prices i.e. when it was added and/or last updated.
 *
 * Only users with the price maintenance role can interact with this controller.
 */
@Controller
@SessionAttributes(
  SUPPLIER_ATTRIBUTE,
  PICK_UP_ATTRIBUTE,
  DROP_OFF_ATTRIBUTE,
  DATE_ATTRIBUTE
)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class MaintainSupplierPricingController(@Autowired val supplierPricingService: SupplierPricingService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  data class PriceForm(
    @get: NotNull(message = "Invalid message id")
    val moveId: String,
    @get: Pattern(regexp = "^[0-9]{1,4}(\\.[0-9]{0,2})?\$", message = "Invalid rate")
    val price: String,
    val from: String?,
    val to: String?
  )

  data class Warning(val text: String) {
    companion object {
      fun default(supplier: Supplier, effectiveYear: Int) = Warning("Please note the added price will be effective for all instances of this journey undertaken by $supplier in the current contractual year $effectiveYear to ${effectiveYear + 1}.")
    }
  }

  @GetMapping("$ADD_PRICE/{moveId}")
  fun addPrice(
    @PathVariable moveId: String,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier
  ): Any {
    logger.info("getting add price for move $moveId")

    val effectiveYear = model.getSelectedEffectiveYear()
    val (fromSite, toSite) = agencyIds(moveId).let { (from, to) ->
      supplierPricingService.getSiteNamesForPricing(
        supplier,
        from,
        to,
        effectiveYear
      )
    }

    model.apply {
      addAttribute("form", PriceForm(moveId, "0.00", fromSite, toSite))
      addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear()))
    }

    return "add-price"
  }

  @PostMapping(ADD_PRICE)
  fun addPrice(
    @Valid @ModelAttribute("form") form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("adding price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    val effectiveYear = model.getSelectedEffectiveYear()

    if (result.hasErrors()) {
      model.addAttribute("warnings", getWarningTexts(supplier, model.getSelectedEffectiveYear()))

      return "add-price"
    }

    agencyIds(form.moveId).let { (from, to) ->
      supplierPricingService.addPriceForSupplier(
        supplier,
        from,
        to,
        price!!,
        effectiveYear
      )
    }

    redirectAttributes.apply {
      addFlashAttribute("flashMessage", "price-created")
      addFlashAttribute("flashAttrLocationFrom", form.from)
      addFlashAttribute("flashAttrLocationTo", form.to)
      addFlashAttribute("flashAttrPrice", form.price)
    }

    return RedirectView(HtmlController.JOURNEYS_URL)
  }

  @GetMapping("$UPDATE_PRICE/{moveId}")
  fun updatePrice(
    @PathVariable moveId: String,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier
  ): String {
    logger.info("getting update price for move $moveId")

    val effectiveYear = model.getSelectedEffectiveYear()

    val (fromAgencyId, toAgencyId) = agencyIds(moveId)

    val (fromSite, toSite, price) = supplierPricingService.getMaybeSiteNamesAndPrice(
      supplier,
      fromAgencyId,
      toAgencyId,
      effectiveYear
    ) ?: throw RuntimeException("No matching price found for $supplier")

    model.apply {
      addAttribute("form", PriceForm(moveId, price.toString(), fromSite, toSite))
      addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear()))
      addAttribute("history", priceHistoryForMove(supplier, fromAgencyId, toAgencyId))
      addAttribute("cancelLink", getJourneySearchResultsUrl())
    }

    return "update-price"
  }

  @PostMapping(UPDATE_PRICE)
  fun updatePrice(
    @Valid @ModelAttribute("form") form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("updating price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    val effectiveYear = model.getSelectedEffectiveYear()

    if (result.hasErrors()) {
      model.apply {
        addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear()))
        addAttribute("cancelLink", getJourneySearchResultsUrl())

        agencyIds(form.moveId).let { (from, to) ->
          addAttribute(
            "history",
            priceHistoryForMove(supplier, from, to)
          )
        }
      }

      return "update-price"
    }

    agencyIds(form.moveId).let { (from, to) ->
      supplierPricingService.updatePriceForSupplier(
        supplier,
        from,
        to,
        price!!,
        effectiveYear
      )
    }

    redirectAttributes.apply {
      addFlashAttribute("flashMessage", "price-updated")
      addFlashAttribute("flashAttrLocationFrom", form.from)
      addFlashAttribute("flashAttrLocationTo", form.to)
      addFlashAttribute("flashAttrPrice", form.price)
    }

    return RedirectView(model.getJourneySearchResultsUrl())
  }

  // TODO check what year is currently selected versus the current contractual year so can derive the warning(s)
  private fun getWarningTexts(supplier: Supplier, effectiveYear: Int) = listOf(Warning.default(supplier, effectiveYear))

  private fun priceHistoryForMove(supplier: Supplier, from: String, to: String) =
    supplierPricingService.priceHistoryForJourney(supplier, from, to)
      .map { history -> PriceHistoryDto.valueOf(supplier, history) }
      .sortedByDescending { lh -> lh.datetime }

  private fun ModelMap.getJourneySearchResultsUrl(): String {
    val url = UriComponentsBuilder.fromUriString(ManageJourneyPriceCatalogueController.SEARCH_JOURNEYS_RESULTS_URL)

    getFromLocation()?.apply { url.fromQueryParam(this) }
    getToLocation()?.apply { url.toQueryParam(this) }

    return url.toUriString()
  }

  private fun agencyIds(combined: String) =
    Pair(combined.split("-")[0].trim().uppercase(), combined.split("-")[1].trim().uppercase())

  private fun parseAmount(value: String) =
    Result.runCatching { value.toDouble() }.getOrNull()?.takeIf { it > 0 }?.let { Money.valueOf(it) }

  private fun ModelMap.getFromLocation() = this.getAttribute(PICK_UP_ATTRIBUTE).takeUnless { it == "" }

  private fun ModelMap.getToLocation() = this.getAttribute(DROP_OFF_ATTRIBUTE).takeUnless { it == "" }

  private fun UriComponentsBuilder.fromQueryParam(from: Any) {
    this.queryParam(PICK_UP_ATTRIBUTE, from)
  }

  private fun UriComponentsBuilder.toQueryParam(to: Any) {
    this.queryParam(DROP_OFF_ATTRIBUTE, to)
  }

  companion object Routes {

    const val ADD_PRICE = "/add-price"
    const val UPDATE_PRICE = "/update-price"

    fun routes(): Array<String> = arrayOf(ADD_PRICE, "$ADD_PRICE/*", UPDATE_PRICE, "$UPDATE_PRICE/*")
  }
}
