package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.NumberFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import javax.validation.Valid
import javax.validation.constraints.Positive

@Controller
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE)
class MaintainSupplierPricingController(@Autowired val supplierPricingService: SupplierPricingService) {

  data class PriceForm(
          val moveId: String,
          @NumberFormat(pattern="#0.00") @get: Positive val price: Double,
          val from: String,
          val to: String)

  @GetMapping("/add-price/{moveId}")
  fun addPrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {
    val ids = agencyIds(moveId)

    val fromAndToSite = supplierPricingService.getSiteNamesForPricing(supplier, ids.first, ids.second)

    model.addAttribute("form", PriceForm(moveId, 0.00, fromAndToSite.first, fromAndToSite.second))
    return "add-price"
  }

  @PostMapping("/add-price")
  fun addPrice(@Valid @ModelAttribute("form") form: PriceForm,
                result: BindingResult,
                model: ModelMap,
                @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {

    if (result.hasErrors()) {
      return "add-price"
    }

    val ids = agencyIds(form.moveId)

    supplierPricingService.addPriceForSupplier(supplier, ids.first, ids.second, form.price)

    return RedirectView(HtmlController.DASHBOARD_URL)
  }

  @GetMapping("/update-price/{moveId}")
  fun updatePrice(@PathVariable moveId: String, model: ModelMap, @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {
    val ids = agencyIds(moveId)

    val sitesAndPrice = supplierPricingService.getExistingSiteNamesAndPrice(supplier, ids.first, ids.second)

    model.addAttribute("form", PriceForm(moveId, sitesAndPrice.third, sitesAndPrice.first, sitesAndPrice.second))

    return "update-price"
  }

  @PostMapping("/update-price")
  fun updatePrice(@Valid @ModelAttribute("form") form: PriceForm,
               result: BindingResult,
               model: ModelMap,
               @ModelAttribute(name = HtmlController.SUPPLIER_ATTRIBUTE) supplier: Supplier): Any {

    if (result.hasErrors()) {
      return "update-price"
    }

    val ids = agencyIds(form.moveId)

    supplierPricingService.updatePriceForSupplier(supplier, ids.first, ids.second, form.price)

    return RedirectView(HtmlController.DASHBOARD_URL)
  }

  private fun agencyIds(combined: String) = Pair(combined.split("-")[0].trim().toUpperCase(), combined.split("-")[1].trim().toUpperCase())
}
