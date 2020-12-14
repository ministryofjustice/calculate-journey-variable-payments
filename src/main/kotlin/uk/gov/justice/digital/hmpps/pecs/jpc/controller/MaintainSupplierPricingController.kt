package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.NumberFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import java.lang.Double.parseDouble
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE, HtmlController.PICK_UP_ATTRIBUTE, HtmlController.DROP_OFF_ATTRIBUTE)
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
    fun addPrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {
        val ids = agencyIds(moveId)

        val fromAndToSite = supplierPricingService.getSiteNamesForPricing(supplier, ids.first, ids.second)

        model.addAttribute("form", PriceForm(moveId, "0.00", fromAndToSite.first, fromAndToSite.second))
        return "add-price"
    }

    @PostMapping("/add-price")
    fun addPrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier, redirectAttributes: RedirectAttributes,
    ): Any {
        val price = this.parseAmount(form.price)

        if (price == null) {
            result.rejectValue("price", "Invalid price")
        }

        if (result.hasErrors()) {
            return "add-price"
        }

        val ids = agencyIds(form.moveId)

        supplierPricingService.addPriceForSupplier(supplier, ids.first, ids.second, Money.valueOf(price!!))

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

        model.addAttribute("form", PriceForm(moveId, sitesAndPrice.third.pounds().toString(), sitesAndPrice.first, sitesAndPrice.second))

        return "update-price"
    }

    @PostMapping("/update-price")
    fun updatePrice(
            @Valid @ModelAttribute("form") form: PriceForm,
            result: BindingResult,
            model: ModelMap,
            @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier, redirectAttributes: RedirectAttributes,
    ): String {
        val price = this.parseAmount(form.price)

        if (price == null) {
            result.rejectValue("price", "Invalid price")
        }

        if (result.hasErrors()) {
            return "update-price"
        }

        val ids = agencyIds(form.moveId)

        supplierPricingService.updatePriceForSupplier(supplier, ids.first, ids.second, Money.valueOf(price!!))

        redirectAttributes.addFlashAttribute("flashMessage", "price-updated")
        redirectAttributes.addFlashAttribute("flashAttrLocationFrom", form.from)
        redirectAttributes.addFlashAttribute("flashAttrLocationTo", form.to)
        redirectAttributes.addFlashAttribute("flashAttrPrice", form.price)

        val from = model.getAttribute(HtmlController.PICK_UP_ATTRIBUTE)
        val to = model.getAttribute(HtmlController.DROP_OFF_ATTRIBUTE)
        val url = UriComponentsBuilder.fromUriString(HtmlController.SEARCH_JOURNEYS_RESULTS_URL)
        if (from != "") {
            url.queryParam(HtmlController.PICK_UP_ATTRIBUTE, from)
        }
        if (to != "") {
            url.queryParam(HtmlController.DROP_OFF_ATTRIBUTE, to)
        }
        return "redirect:${url.build().toUri()}"
    }

    private fun agencyIds(combined: String) = Pair(combined.split("-")[0].trim().toUpperCase(), combined.split("-")[1].trim().toUpperCase())
    private fun parseAmount(value: String): Double? {
        var price: Double = 0.0
        try {
            price = parseDouble(value)
        } catch (e: NumberFormatException) {
            return null
        }
        return if (price >= 0) price else null
    }
}
