package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Autowired
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
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE, HtmlController.PICK_UP_ATTRIBUTE, HtmlController.DROP_OFF_ATTRIBUTE, HtmlController.DATE_ATTRIBUTE)
class MaintainSupplierPricingController(@Autowired val supplierPricingService: SupplierPricingService) {

    data class PriceForm(
            @get: NotNull(message = "Invalid message id")
            val moveId: String,
            @get: NotNull(message = "Add a price")
            val price: String,
            val from: String?,
            val to: String?,
    )

    @GetMapping("/add-price/{moveId}")
    fun addPrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier, ): Any {
        val (fromSite, toSite) = agencyIds(moveId).let { (from, to) -> supplierPricingService.getSiteNamesForPricing(supplier, from, to) }
        val effectiveYear = effectiveYearForDate(model.getStartOfMonth())

        model.apply {
            addAttribute("form", PriceForm(moveId, "0.00", fromSite, toSite))
            addAttribute("contractualYearStart", "$effectiveYear")
            addAttribute("contractualYearEnd", "${effectiveYear + 1}")
        }

        return "add-price"
    }

    @PostMapping("/add-price")
    fun addPrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
            redirectAttributes: RedirectAttributes, ): Any {

        val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

        if (result.hasErrors()) {
            val effectiveYear = effectiveYearForDate(model.getStartOfMonth())

            model.addAttribute("contractualYearStart", "$effectiveYear")
            model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")

            return "add-price"
        }

        agencyIds(form.moveId).let { (from, to) -> supplierPricingService.addPriceForSupplier(supplier, from, to, price!!) }

        redirectAttributes.apply {
            addFlashAttribute("flashMessage", "price-created")
            addFlashAttribute("flashAttrLocationFrom", form.from)
            addFlashAttribute("flashAttrLocationTo", form.to)
            addFlashAttribute("flashAttrPrice", form.price)
        }

        return RedirectView(HtmlController.JOURNEYS_URL)
    }

    @GetMapping("/update-price/{moveId}")
    fun updatePrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): String {
        val (fromSite, toSite, price) = agencyIds(moveId).let { (from, to) -> supplierPricingService.getExistingSiteNamesAndPrice(supplier, from, to) }
        val startOfMonth = model.getStartOfMonth()
        val effectiveYear = effectiveYearForDate(startOfMonth)

        model.apply {
            addAttribute("form", PriceForm(moveId, price.pounds().toString(), fromSite, toSite))
            addAttribute("contractualYearStart", "$effectiveYear")
            addAttribute("contractualYearEnd", "${effectiveYear + 1}")
        }

        model.addAttribute("cancelLink", model.getJourneySearchResultsUrl())

        return "update-price"
    }

    @PostMapping("/update-price")
    fun updatePrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
            redirectAttributes: RedirectAttributes, ): Any {

        val price = parseAmount(form.price).also { if (it == null) result.rejectValue("price", "Invalid price") }

        if (result.hasErrors()) {
            model.addAttribute("cancelLink", model.getJourneySearchResultsUrl())
            return "update-price"
        }

        agencyIds(form.moveId).let { (from, to) ->  supplierPricingService.updatePriceForSupplier(supplier, from, to, price!!)}

        redirectAttributes.apply {
            addFlashAttribute("flashMessage", "price-updated")
            addFlashAttribute("flashAttrLocationFrom", form.from)
            addFlashAttribute("flashAttrLocationTo", form.to)
            addFlashAttribute("flashAttrPrice", form.price)
        }

        return RedirectView(model.getJourneySearchResultsUrl())
    }

    private fun ModelMap.getJourneySearchResultsUrl(): String {
        val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

        getFromLocation()?.apply { url.fromQueryParam( this) }
        getToLocation()?.apply { url.toQueryParam( this) }

        return url.build().toUriString()
    }

    private fun agencyIds(combined: String) = Pair(combined.split("-")[0].trim().toUpperCase(), combined.split("-")[1].trim().toUpperCase())

    private fun parseAmount(value: String) = Result.runCatching { value.toDouble() }.getOrNull()?.takeIf { it > 0 }?.let { Money.valueOf(it) }

    private fun ModelMap.getFromLocation() = this.getAttribute(HtmlController.PICK_UP_ATTRIBUTE).takeUnless { it == "" }

    private fun ModelMap.getToLocation() = this.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE).takeUnless { it == "" }

    private fun ModelMap.getStartOfMonth() = this.getAttribute(HtmlController.DATE_ATTRIBUTE) as LocalDate

    private fun UriComponentsBuilder.fromQueryParam(from: Any) { this.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from)}

    private fun UriComponentsBuilder.toQueryParam(to: Any) { this.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to)}
}
