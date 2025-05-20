package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.ordinalMonthsAndYearForSeptemberToAugust
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.SupplierPricingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.math.BigDecimal
import java.time.Month

/**
 * Controller to handle requests for Supplier price maintenance. This includes adding new prices, updating existing
 * prices, adding/remove price exceptions and displaying the history of prices i.e. when it was added and/or last
 * updated.
 *
 * Only users with the price maintenance role can interact with this controller. This is normally the commercial users.
 */
private val logger = loggerFor<MaintainSupplierPricingController>()

@Controller
@SessionAttributes(
  SUPPLIER_ATTRIBUTE,
  PICK_UP_ATTRIBUTE,
  DROP_OFF_ATTRIBUTE,
  DATE_ATTRIBUTE,
)
@PreAuthorize("hasRole('PECS_MAINTAIN_PRICE')")
class MaintainSupplierPricingController(
  @Autowired val supplierPricingService: SupplierPricingService,
  @Autowired val actualEffectiveYear: EffectiveYear,
) : PrimaryNavigationBar {

  override fun primaryNavigationChoice() = PrimaryNavigation.PRICE

  data class PriceForm(
    @get:NotNull(message = "Invalid message id")
    val moveId: String,
    @get:Pattern(regexp = "^[0-9]{1,4}(\\.[0-9]{0,2})?\$", message = "Invalid price")
    val price: String,
    val from: String?,
    val to: String?,
  )

  data class Warning(val text: String) {
    companion object {
      fun standard(supplier: Supplier, effectiveYear: Int) = Warning("Please note the added price will be effective for all instances of this journey undertaken by $supplier in the current contractual year $effectiveYear to ${effectiveYear + 1}.")

      fun beforeWithCurrentPrice(effectiveYear: Int) = Warning("Please note a price for this journey already exists in the current contractual year $effectiveYear to ${effectiveYear + 1}.")

      fun before(effectiveYear: Int) = Warning("Making this change will only affect journeys undertaken in the contractual year $effectiveYear to ${effectiveYear + 1}. You will need to apply a bulk price adjustment to calculate the new journey price in the current contractual year.")
    }
  }

  data class PriceExceptionForm(
    val moveId: String,
    val existingExceptions: Map<Int, Money> = emptyMap(),
    val exceptionMonth: String? = null,
    val effectiveYear: Int? = null,
    @get:Pattern(regexp = "^[0-9]{1,4}(\\.[0-9]{0,2})?\$", message = "Invalid price")
    val exceptionPrice: String? = null,
  ) {
    val months: List<PriceExceptionMonth> = effectiveYear?.let {
      ordinalMonthsAndYearForSeptemberToAugust(effectiveYear).map { (month, year) ->
        PriceExceptionMonth(
          Month.of(month),
          existingExceptions.containsKey(month),
          year,
          existingExceptions[month],
        )
      }
    } ?: listOf()
  }

  data class PriceExceptionMonth(
    val value: String,
    val month: String,
    val alreadySelected: Boolean,
    val year: Int,
    val amount: Money?,
  ) {
    constructor(month: Month, alreadySelected: Boolean, year: Int, amount: Money?) : this(
      month.name,
      month.name.titleCased(),
      alreadySelected,
      year,
      amount,
    )
  }

  @GetMapping("$ADD_PRICE/{moveId}")
  fun addPrice(
    @PathVariable moveId: String,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("getting add price for move $moveId")

    if (actualEffectiveYear.canAddOrUpdatePrices(model.getSelectedEffectiveYear())) {
      val effectiveYear = model.getSelectedEffectiveYear()
      val (fromAgencyId, toAgencyId) = agencyIds(moveId)

      val (fromSite, toSite) =
        supplierPricingService.getSiteNamesForPricing(
          supplier,
          fromAgencyId,
          toAgencyId,
          effectiveYear,
        )

      model.apply {
        addAttribute("form", PriceForm(moveId, "0.00", fromSite, toSite))
        addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear(), fromAgencyId, toAgencyId))
        addContractStartAndEndDates()
      }

      return "add-price"
    }

    return RedirectView(SummaryPageController.DASHBOARD_URL)
  }

  @PostMapping(ADD_PRICE)
  fun addPrice(
    @Valid
    @ModelAttribute("form")
    form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("adding price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    val effectiveYear = model.getSelectedEffectiveYear()

    val (fromAgencyId, toAgencyId) = agencyIds(form.moveId)

    if (result.hasErrors()) {
      model.addAttribute(
        "warnings",
        getWarningTexts(supplier, model.getSelectedEffectiveYear(), fromAgencyId, toAgencyId),
      )

      model.addContractStartAndEndDates()

      return "add-price"
    }

    supplierPricingService.addPriceForSupplier(
      supplier,
      fromAgencyId,
      toAgencyId,
      price!!,
      effectiveYear,
    )

    redirectAttributes.showPriceCreatedMessageOnRedirect(form)

    return RedirectView(SummaryPageController.JOURNEYS_URL)
  }

  private fun RedirectAttributes.showPriceCreatedMessageOnRedirect(form: PriceForm) {
    this.apply {
      addFlashAttribute("flashMessage", "price-created")
      addFlashAttribute("flashAttrLocationFrom", form.from)
      addFlashAttribute("flashAttrLocationTo", form.to)
      addFlashAttribute("flashAttrPrice", form.price)
    }
  }

  @GetMapping("$UPDATE_PRICE/{moveId}")
  fun updatePrice(
    @PathVariable moveId: String,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
  ): String {
    logger.info("getting update price for move $moveId")

    val effectiveYear = model.getSelectedEffectiveYear()

    val (fromAgencyId, toAgencyId) = agencyIds(moveId)

    val price = supplierPricingService.maybePrice(
      supplier,
      fromAgencyId,
      toAgencyId,
      effectiveYear,
    ) ?: throw RuntimeException("No matching price found for $supplier")

    if (actualEffectiveYear.canAddOrUpdatePrices(model.getSelectedEffectiveYear())) {
      model.apply {
        addAttribute("form", PriceForm(moveId, price.amount.toString(), price.fromAgency, price.toAgency))
        addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear(), fromAgencyId, toAgencyId))
        addAttribute("history", priceHistoryForMove(supplier, fromAgencyId, toAgencyId))
        addAttribute("cancelLink", getJourneySearchResultsUrl())
        addAttribute("existingExceptions", existingExceptions(price.exceptions, getSelectedEffectiveYear()))
        addAttribute(
          "exceptionsForm",
          PriceExceptionForm(
            moveId,
            price.exceptions,
            exceptionPrice = "0.00",
            effectiveYear = getSelectedEffectiveYear(),
          ),
        )
        addContractStartAndEndDates()
      }

      return "update-price"
    }

    model.apply {
      addAttribute("history", priceHistoryForMove(supplier, fromAgencyId, toAgencyId))
      addAttribute("cancelLink", getJourneySearchResultsUrl())
      addContractStartAndEndDates()
    }

    return "update-price-history"
  }

  @PostMapping(UPDATE_PRICE)
  fun updatePrice(
    @Valid
    @ModelAttribute("form")
    form: PriceForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("updating price for move $supplier")

    val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

    if (result.hasErrors()) {
      val (fromAgencyId, toAgencyId) = agencyIds(form.moveId)

      model.apply {
        val existingPrice =
          supplierPricingService.maybePrice(supplier, fromAgencyId, toAgencyId, getSelectedEffectiveYear())!!

        addAttribute("warnings", getWarningTexts(supplier, getSelectedEffectiveYear(), fromAgencyId, toAgencyId))
        addAttribute("cancelLink", getJourneySearchResultsUrl())
        addAttribute("history", priceHistoryForMove(supplier, fromAgencyId, toAgencyId))
        addAttribute("existingExceptions", existingExceptions(existingPrice.exceptions, getSelectedEffectiveYear()))
        addAttribute(
          "exceptionsForm",
          PriceExceptionForm(form.moveId, existingPrice.exceptions, effectiveYear = getSelectedEffectiveYear()),
        )
        addContractStartAndEndDates()
      }

      return "update-price"
    }

    agencyIds(form.moveId).let { (from, to) ->
      supplierPricingService.updatePriceForSupplier(
        supplier,
        from,
        to,
        price!!,
        model.getSelectedEffectiveYear(),
      )
    }

    redirectAttributes.showPriceUpdatedMessageOnRedirect(form)

    return RedirectView(model.getJourneySearchResultsUrl())
  }

  private fun RedirectAttributes.showPriceUpdatedMessageOnRedirect(form: PriceForm) {
    this.apply {
      addFlashAttribute("flashMessage", "price-updated")
      addFlashAttribute("flashAttrLocationFrom", form.from)
      addFlashAttribute("flashAttrLocationTo", form.to)
      addFlashAttribute("flashAttrPrice", form.price)
    }
  }

  private fun existingExceptions(existingExceptions: Map<Int, Money>, effectiveYear: Int) = ordinalMonthsAndYearForSeptemberToAugust(effectiveYear).mapNotNull { (month, year) ->
    if (existingExceptions.containsKey(month)) {
      PriceExceptionMonth(
        Month.of(month),
        existingExceptions.containsKey(month),
        year,
        existingExceptions[month],
      )
    } else {
      null
    }
  }

  @PostMapping(ADD_PRICE_EXCEPTION)
  fun addPriceException(
    @Valid
    @ModelAttribute("exceptionsForm")
    form: PriceExceptionForm,
    result: BindingResult,
    model: ModelMap,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    redirectAttributes: RedirectAttributes,
  ): Any {
    logger.info("Adding price exception")

    val price =
      parseAmount(form.exceptionPrice!!).also { if (it == null) result.rejectValue("exceptionPrice", "Invalid price") }

    val (fromAgencyId, toAgencyId) = agencyIds(form.moveId)

    val existingPrice = supplierPricingService.maybePrice(
      supplier,
      fromAgencyId,
      toAgencyId,
      model.getSelectedEffectiveYear(),
    )!!

    if (existingPrice.amount == price) result.rejectValue("exceptionPrice", "Invalid price")

    if (result.hasErrors()) {
      redirectAttributes.showErrorOnRedirect("add-price-exception-error")

      return RedirectView("$UPDATE_PRICE/${form.moveId}#price-exceptions")
    }

    supplierPricingService.addPriceException(
      supplier,
      fromAgencyId,
      toAgencyId,
      model.getSelectedEffectiveYear(),
      Month.valueOf(form.exceptionMonth!!),
      price!!,
    )

    redirectAttributes.apply {
      addFlashAttribute("flashMessage", "price-exception-created")
      addFlashAttribute("flashAttrExceptionPrice", form.exceptionPrice)
      addFlashAttribute("flashAttrExceptionMonth", form.exceptionMonth)
      addFlashAttribute("flashAttrLocationFrom", existingPrice.fromAgency)
      addFlashAttribute("flashAttrLocationTo", existingPrice.toAgency)
    }

    return RedirectView("$UPDATE_PRICE/${form.moveId}#price-exceptions")
  }

  @PostMapping(REMOVE_PRICE_EXCEPTION)
  fun removePriceException(
    moveId: String,
    month: String,
    model: ModelMap,
    redirectAttributes: RedirectAttributes,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
  ): Any {
    val price = agencyIds(moveId).run {
      supplierPricingService.removePriceException(
        supplier,
        this.first,
        this.second,
        model.getSelectedEffectiveYear(),
        Month.valueOf(month),
      )
    }

    redirectAttributes.apply {
      addFlashAttribute("flashMessage", "price-exception-removed")
      addFlashAttribute("flashAttrLocationFrom", price.fromAgency)
      addFlashAttribute("flashAttrLocationTo", price.toAgency)
      addFlashAttribute("flashAttrExceptionMonth", month)
    }

    return RedirectView("$UPDATE_PRICE/$moveId#price-exceptions")
  }

  private fun RedirectAttributes.showErrorOnRedirect(attribute: String) = this.addFlashAttribute("flashError", attribute)

  private fun getWarningTexts(supplier: Supplier, selectedEffectiveYear: Int, from: String, to: String): List<Warning> {
    if (selectedEffectiveYear >= actualEffectiveYear.current()) {
      return listOf(Warning.standard(supplier, selectedEffectiveYear))
    }

    supplierPricingService.maybePrice(supplier, from, to, actualEffectiveYear.current())?.let {
      return listOf(
        Warning.beforeWithCurrentPrice(actualEffectiveYear.current()),
        Warning.before(selectedEffectiveYear),
      )
    }

    return listOf(Warning.before(selectedEffectiveYear))
  }

  private fun priceHistoryForMove(supplier: Supplier, from: String, to: String) = supplierPricingService.priceHistoryForJourney(supplier, from, to)
    .map { history -> PriceHistoryDto.valueOf(supplier, history) }
    .sortedByDescending { lh -> lh.datetime }

  private fun ModelMap.getJourneySearchResultsUrl(): String {
    val url = UriComponentsBuilder.fromUriString(ManageJourneyPriceCatalogueController.SEARCH_JOURNEYS_RESULTS_URL)

    getFromLocation()?.apply { url.fromQueryParam(this) }
    getToLocation()?.apply { url.toQueryParam(this) }

    return url.toUriString()
  }

  private fun agencyIds(combined: String) = Pair(combined.split("-")[0].trim().uppercase(), combined.split("-")[1].trim().uppercase())

  private fun parseAmount(value: String) = value.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }?.let { Money.valueOf(it) }

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
    const val ADD_PRICE_EXCEPTION = "/add-price-exception"
    const val REMOVE_PRICE_EXCEPTION = "/remove-price-exception"
    const val UPDATE_PRICE = "/update-price"
  }
}
