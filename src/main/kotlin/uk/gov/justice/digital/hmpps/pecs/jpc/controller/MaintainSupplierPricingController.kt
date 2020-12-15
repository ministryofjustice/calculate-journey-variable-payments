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
        val ids = agencyIds(moveId)

        val fromAndToSite = supplierPricingService.getSiteNamesForPricing(supplier, ids.first, ids.second)
        val startOfMonth = model.getAttribute(HtmlController.DATE_ATTRIBUTE) as LocalDate
        val effectiveYear = effectiveYearForDate(startOfMonth)

        model.addAttribute("form", PriceForm(moveId, "0.00", fromAndToSite.first, fromAndToSite.second))
        model.addAttribute("contractualYearStart", "${effectiveYear}")
        model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")
        return "add-price"
    }

    @PostMapping("/add-price")
    fun addPrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
            redirectAttributes: RedirectAttributes, ): Any {
        val price = parseAmount(form.price)

        if (price == null) {
            result.rejectValue("price", "Invalid price")
        }

        if (result.hasErrors()) {
            return "add-price"
        }

        val ids = agencyIds(form.moveId)

        supplierPricingService.addPriceForSupplier(supplier, ids.first, ids.second, price!!)

        redirectAttributes.addFlashAttribute("flashMessage", "price-created")
        redirectAttributes.addFlashAttribute("flashAttrLocationFrom", form.from)
        redirectAttributes.addFlashAttribute("flashAttrLocationTo", form.to)
        redirectAttributes.addFlashAttribute("flashAttrPrice", form.price)
        return RedirectView(HtmlController.JOURNEYS_URL)
    }

    @GetMapping("/update-price/{moveId}")
    fun updatePrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): String {
        val ids = agencyIds(moveId)

        val sitesAndPrice = supplierPricingService.getExistingSiteNamesAndPrice(supplier, ids.first, ids.second)
        val startOfMonth = model.getAttribute(HtmlController.DATE_ATTRIBUTE) as LocalDate
        val effectiveYear = effectiveYearForDate(startOfMonth)

        model.addAttribute("form", PriceForm(moveId, sitesAndPrice.third.pounds().toString(), sitesAndPrice.first, sitesAndPrice.second))
        model.addAttribute("contractualYearStart", "${effectiveYear}")
        model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")

        val from = model.getAttribute(HtmlController.PICK_UP_ATTRIBUTE)
        val to = model.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE)
        val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

        from.takeUnless { it == "" }.apply { url.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from) }
        to.takeUnless { it == "" }.apply { url.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to) }

        model.addAttribute("cancelLink", url.build().toUriString())

        return "update-price"
    }

    @PostMapping("/update-price")
    fun updatePrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier,
            redirectAttributes: RedirectAttributes, ): Any {
        val price = parseAmount(form.price)

        if (price == null) {
            result.rejectValue("price", "Invalid price")
        }

        if (result.hasErrors()) {
            return "update-price"
        }

        val ids = agencyIds(form.moveId)

        supplierPricingService.updatePriceForSupplier(supplier, ids.first, ids.second, price!!)

        redirectAttributes.addFlashAttribute("flashMessage", "price-updated")
        redirectAttributes.addFlashAttribute("flashAttrLocationFrom", form.from)
        redirectAttributes.addFlashAttribute("flashAttrLocationTo", form.to)
        redirectAttributes.addFlashAttribute("flashAttrPrice", form.price)

        val from = model.getAttribute(HtmlController.PICK_UP_ATTRIBUTE)
        val to = model.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE)
        val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)

        from.takeUnless { it == "" }.apply { url.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from) }
        to.takeUnless { it == "" }.apply { url.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to) }

        return RedirectView(url.build().toUriString())
    }

    private fun agencyIds(combined: String) = Pair(combined.split("-")[0].trim().toUpperCase(), combined.split("-")[1].trim().toUpperCase())

    private fun parseAmount(value: String): Money? {
        return Result.runCatching { value.toDouble() }.getOrNull()?.takeIf { it >= 0 }?.let { Money.valueOf(it) }
    }
}
