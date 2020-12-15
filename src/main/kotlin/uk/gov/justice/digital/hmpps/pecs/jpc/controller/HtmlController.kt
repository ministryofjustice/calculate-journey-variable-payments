package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidJourneySearch
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.DROP_OFF_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.END_OF_MONTH_DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.PICK_UP_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.START_OF_MONTH_DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.SUPPLIER_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser
import java.time.LocalDate
import javax.validation.Valid


data class MonthsWidget(val currentMonth: LocalDate, val nextMonth: LocalDate, val previousMonth: LocalDate)

@Controller
@SessionAttributes(SUPPLIER_ATTRIBUTE, DATE_ATTRIBUTE, START_OF_MONTH_DATE_ATTRIBUTE, END_OF_MONTH_DATE_ATTRIBUTE, PICK_UP_ATTRIBUTE, DROP_OFF_ATTRIBUTE)
class HtmlController(@Autowired val moveService: MoveService, @Autowired val journeyService: JourneyService) {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView(DASHBOARD_URL)
    }

    @RequestMapping("/choose-supplier")
    fun chooseSupplier(model: ModelMap): String {
        return "choose-supplier"
    }

    @RequestMapping("/choose-supplier/serco")
    fun chooseSupplierSerco(model: ModelMap): RedirectView {
        model.addAttribute(SUPPLIER_ATTRIBUTE, Supplier.SERCO)
        return RedirectView(DASHBOARD_URL)
    }

    @RequestMapping("/choose-supplier/geoamey")
    fun chooseSupplierGeoAmey(model: ModelMap): RedirectView {
        model.addAttribute(SUPPLIER_ATTRIBUTE, Supplier.GEOAMEY)
        return RedirectView(DASHBOARD_URL)
    }

    @RequestMapping("$MOVES_BY_TYPE_URL/{moveTypeString}")
    fun movesById(@PathVariable moveTypeString: String, @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): String {
        val moveType = MoveType.valueOfCaseInsensitive(moveTypeString)
        val moves = moveService.movesForMoveType(supplier, moveType, startOfMonth)
        val moveTypeSummary = moveService.summaryForMoveType(supplier, moveType, startOfMonth)

        model.addAttribute("months", MonthsWidget((startOfMonth), nextMonth = (startOfMonth.plusMonths(1)), previousMonth = (startOfMonth.minusMonths(1))))
        model.addAttribute("summary", moveTypeSummary.movesSummary)
        model.addAttribute("moves", moves)
        model.addAttribute("moveType", moveType.text)
        return "moves-by-type"
    }

    @RequestMapping("$MOVES_URL/{moveId}")
    fun moves(@PathVariable moveId: String, model: ModelMap): String {
        val move = moveService.moveWithPersonJourneysAndEvents(moveId)
        model.addAttribute(MOVE_ATTRIBUTE, move)
        return "move"
    }

    @RequestMapping(JOURNEYS_URL)
    fun journeys(
            @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate,
            @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
            @ModelAttribute(name = "flashAttrMappedLocationName") locationName: String?,
            @ModelAttribute(name = "flashAttrMappedAgencyId") agencyId: String?,
            model: ModelMap,
    ): String {

        removeAttributesIf(locationName.isNullOrEmpty(), model, "flashAttrMappedLocationName", "flashAttrMappedAgencyId")

        model.addAttribute("journeysSummary", journeyService.journeysSummary(supplier, startOfMonth))
        model.addAttribute("journeys", journeyService.distinctJourneysExcludingPriced(supplier, startOfMonth))

        return "journeys"
    }

    private fun removeAttributesIf(condition: Boolean, model: ModelMap, vararg attributeNames: String) {
        if (condition) attributeNames.forEach { model.remove(it) }
    }

    @RequestMapping(DASHBOARD_URL)
    fun dashboard(@RequestParam(name = DATE_ATTRIBUTE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) requestParamStartOfMonth: LocalDate?, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): Any {
        requestParamStartOfMonth?.let {
            model.addAttribute(DATE_ATTRIBUTE, requestParamStartOfMonth)
        }
        val startOfMonth = model.getAttribute(DATE_ATTRIBUTE) as LocalDate
        val endOfMonth = endOfMonth(startOfMonth)
        model.addAttribute(START_OF_MONTH_DATE_ATTRIBUTE, startOfMonth)
        model.addAttribute(END_OF_MONTH_DATE_ATTRIBUTE, endOfMonth)

        val countAndSummaries = moveService.moveTypeSummaries(supplier, startOfMonth)
        val journeysSummary = journeyService.journeysSummary(supplier, startOfMonth)

        model.addAttribute("months", MonthsWidget((startOfMonth), nextMonth = (startOfMonth.plusMonths(1)), previousMonth = (startOfMonth.minusMonths(1))))
        model.addAttribute("summary", countAndSummaries.summary())
        model.addAttribute("journeysSummary", journeysSummary)
        model.addAttribute("summaries", countAndSummaries.allSummaries())
        return "dashboard"
    }

    @GetMapping(SELECT_MONTH_URL)
    fun selectMonth(@ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): Any {
        model.addAttribute("form", JumpToMonthForm(date = ""))
        return "select-month"
    }

    data class JumpToMonthForm(@ValidMonthYear val date: String)

    @PostMapping(SELECT_MONTH_URL)
    fun jumpToMonth(@Valid @ModelAttribute("form") form: JumpToMonthForm, result: BindingResult, model: ModelMap): Any {
        if (result.hasErrors()) {
            return "select-month"
        }

        model.addAttribute(DATE_ATTRIBUTE, MonthYearParser.atStartOf(form.date))

        return RedirectView(DASHBOARD_URL)
    }

    @GetMapping(SEARCH_JOURNEYS_URL)
    fun searchJourneys(model: ModelMap): Any {
        val startOfMonth = model.getAttribute(HtmlController.DATE_ATTRIBUTE) as LocalDate
        val effectiveYear = effectiveYearForDate(startOfMonth)

        model.addAttribute("form", SearchJourneyForm())
        model.addAttribute("contractualYearStart", "${effectiveYear}")
        model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")
        return "search-journeys"
    }

    @ValidJourneySearch
    data class SearchJourneyForm(val from: String? = null, val to: String? = null)

    @PostMapping(SEARCH_JOURNEYS_URL)
    fun performJourneySearch(
            @Valid @ModelAttribute("form") form: SearchJourneyForm,
            result: BindingResult,
            @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
            model: ModelMap, redirectAttributes: RedirectAttributes,
    ): String {

        if (result.hasErrors()) {
            return "search-journeys"
        }
        val from = form.from?.trim() ?: ""
        val to = form.to?.trim() ?: ""

        val url = UriComponentsBuilder.fromUriString(SEARCH_JOURNEYS_RESULTS_URL)
        if (from != "") {
            url.queryParam(PICK_UP_ATTRIBUTE, from)
        }
        if (to != "") {
            url.queryParam(DROP_OFF_ATTRIBUTE, to)
        }

        model.addAttribute(PICK_UP_ATTRIBUTE, from)
        model.addAttribute(DROP_OFF_ATTRIBUTE, to)

        return "redirect:${url.build().toUri()}"
    }

    @GetMapping(SEARCH_JOURNEYS_RESULTS_URL)
    fun searchJourneys(@RequestParam(name = PICK_UP_ATTRIBUTE, required = false) pickUpLocation: String?, @RequestParam(name = DROP_OFF_ATTRIBUTE, required = false) dropOffLocation: String?, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): Any {
        if ((pickUpLocation == null || pickUpLocation == "") && (dropOffLocation == null || dropOffLocation == "")) {
            return RedirectView(SEARCH_JOURNEYS_URL)
        }

        val startOfMonth = model.getAttribute(DATE_ATTRIBUTE) as LocalDate
        val effectiveYear = effectiveYearForDate(startOfMonth)
        val journeys = journeyService.prices(supplier, pickUpLocation, dropOffLocation, effectiveYear)

        model.addAttribute("contractualYearStart", "${effectiveYear}")
        model.addAttribute("contractualYearEnd", "${effectiveYear + 1}")

        if (journeys.isEmpty()) {
            model.addAttribute("pickUpLocation", pickUpLocation ?: "")
            model.addAttribute("dropOffLocation", dropOffLocation ?: "")
            return "no-search-journeys-results"
        } else {
            model.addAttribute("journeys", journeys)
            return "search-journeys-results"
        }
    }

    companion object {
        const val PICK_UP_ATTRIBUTE = "pick-up"
        const val DROP_OFF_ATTRIBUTE = "drop-off"
        const val DATE_ATTRIBUTE = "date"
        const val SUPPLIER_ATTRIBUTE = "supplier"
        const val START_OF_MONTH_DATE_ATTRIBUTE = "startOfMonthDate"
        const val END_OF_MONTH_DATE_ATTRIBUTE = "endOfMonthDate"
        const val MOVE_ATTRIBUTE = "move"

        const val DASHBOARD_URL = "/dashboard"
        const val SELECT_MONTH_URL = "/select-month"
        const val MOVES_BY_TYPE_URL = "/moves-by-type"
        const val MOVES_URL = "/moves"
        const val JOURNEYS_URL = "/journeys"
        const val SEARCH_JOURNEYS_URL = "/search-journeys"
        const val SEARCH_JOURNEYS_RESULTS_URL = "/journeys-results"
    }
}
