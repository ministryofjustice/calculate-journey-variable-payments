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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller
@SessionAttributes(
  HtmlController.SUPPLIER_ATTRIBUTE,
  HtmlController.PICK_UP_ATTRIBUTE,
  HtmlController.DROP_OFF_ATTRIBUTE,
  HtmlController.DATE_ATTRIBUTE
)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class MaintainSupplierPricingController(@Autowired val supplierPricingService: SupplierPricingService) {

  private val logger = LoggerFactory.getLogger(javaClass)

  data class PriceForm(
    @get: NotNull(message = "Invalid message id")
    val moveId: String,
    @get: NotNull(message = "Add a price")
    val price: String,
    val from: String?,
    val to: String?,
  )

  @GetMapping("$ADD_PRICE/{moveId}")
  fun addPrice(
    @PathVariable moveId: String,
    model: ModelMap,
    @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier
  ): Any {
    logger.info("getting add price for move $moveId")

    val effectiveYear = model.getEffectiveYear()
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
      addAttribute("contractualYearStart", "$effectiveYear")
      addAttribute("contractualYearEnd", "${effectiveYear + 1}")
    }

    return "add-price"
  }

  @PostMapping(ADD_PRICE)
  fun addPrice(
    @Valid @ModelAttribute("form") form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("adding price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    val effectiveYear = model.getEffectiveYear()
    if (result.hasErrors()) {
      model.addAttribute("contractualYearStart", "$effectiveYear")
      model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")

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
    @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier
  ): String {
    logger.info("getting update price for move $moveId")

    val effectiveYear = model.getEffectiveYear()

    val (fromAgencyId, toAgencyId) = agencyIds(moveId)

    val (fromSite, toSite, price) = supplierPricingService.getExistingSiteNamesAndPrice(
      supplier,
      fromAgencyId,
      toAgencyId,
      effectiveYear
    )

    model.apply {
      addAttribute("form", PriceForm(moveId, price.toString(), fromSite, toSite))
      addAttribute("contractualYearStart", "$effectiveYear")
      addAttribute("contractualYearEnd", "${effectiveYear + 1}")
      addAttribute("history", priceHistoryForMove(supplier, fromAgencyId, toAgencyId))
    }

    model.addAttribute("cancelLink", model.getJourneySearchResultsUrl())

    return "update-price"
  }

  @PostMapping(UPDATE_PRICE)
  fun updatePrice(
    @Valid @ModelAttribute("form") form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("updating price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    val effectiveYear = model.getEffectiveYear()

    if (result.hasErrors()) {
      model.addAttribute("contractualYearStart", "$effectiveYear")
      model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")
      model.addAttribute("cancelLink", model.getJourneySearchResultsUrl())

      agencyIds(form.moveId).let { (from, to) ->
        model.addAttribute(
          "history",
          priceHistoryForMove(supplier, from, to)
        )
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

  private fun priceHistoryForMove(supplier: Supplier, from: String, to: String) =
    supplierPricingService.priceHistoryForJourney(supplier, from, to)
      .map { history -> PriceHistoryDto.valueOf(supplier, history) }
      .sortedByDescending { lh -> lh.datetime }

  private fun ModelMap.getJourneySearchResultsUrl(): String {
    val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

    getFromLocation()?.apply { url.fromQueryParam(this) }
    getToLocation()?.apply { url.toQueryParam(this) }

    return url.toUriString()
  }

  private fun agencyIds(combined: String) =
    Pair(combined.split("-")[0].trim().uppercase(), combined.split("-")[1].trim().uppercase())

  private fun parseAmount(value: String) =
    Result.runCatching { value.toDouble() }.getOrNull()?.takeIf { it > 0 }?.let { Money.valueOf(it) }

  private fun ModelMap.getFromLocation() = this.getAttribute(HtmlController.PICK_UP_ATTRIBUTE).takeUnless { it == "" }

  private fun ModelMap.getToLocation() = this.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE).takeUnless { it == "" }

  private fun ModelMap.getEffectiveYear() =
    effectiveYearForDate(this.getAttribute(HtmlController.DATE_ATTRIBUTE) as LocalDate)

  private fun UriComponentsBuilder.fromQueryParam(from: Any) {
    this.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from)
  }

  private fun UriComponentsBuilder.toQueryParam(to: Any) {
    this.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to)
  }

  companion object Routes {

    const val ADD_PRICE = "/add-price"
    const val UPDATE_PRICE = "/update-price"

    fun routes(): Array<String> = arrayOf(ADD_PRICE, "$ADD_PRICE/*", UPDATE_PRICE, "$UPDATE_PRICE/*")
  }
}
