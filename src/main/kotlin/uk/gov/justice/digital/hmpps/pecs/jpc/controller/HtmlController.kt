package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.END_OF_MONTH_DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.START_OF_MONTH_DATE_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.SUPPLIER_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.validation.Valid


data class MonthsWidget(val currentMonth: Date, val nextMonth: Date, val previousMonth: Date)

@Controller
@SessionAttributes(SUPPLIER_ATTRIBUTE, DATE_ATTRIBUTE, START_OF_MONTH_DATE_ATTRIBUTE, END_OF_MONTH_DATE_ATTRIBUTE )
class HtmlController(@Autowired val moveService: MoveService) {

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
    fun movesById(@PathVariable moveTypeString:String, @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): String {
        val moveType = MoveType.valueOfCaseInsensitive(moveTypeString)
        val moves = moveService.movesForMoveType(supplier, moveType, startOfMonth)
        val moveTypeSummary = moveService.summaryForMoveType(supplier, moveType, startOfMonth)

        model.addAttribute("months", MonthsWidget(convertToDate(startOfMonth), nextMonth = convertToDate(startOfMonth.plusMonths(1)), previousMonth = convertToDate(startOfMonth.minusMonths(1))))
        model.addAttribute("summary", moveTypeSummary.movesSummary)
        model.addAttribute("moves", moves)
        return "moves-by-type"
    }

    @RequestMapping("$MOVES_URL/{moveId}")
    fun moves(@PathVariable moveId: String, model: ModelMap) : String{
        val move = moveService.move(moveId)
        model.addAttribute(MOVE_ATTRIBUTE, move)
        return "move"
    }

    @RequestMapping(JOURNEYS_URL)
    fun journeys(
            @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate,
            @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
            model: ModelMap) : String{

        model.addAttribute("journeysSummary", moveService.journeysSummary(supplier, startOfMonth))
        model.addAttribute("journeys", moveService.uniqueJourneys(supplier, startOfMonth))
        return "journeys"
    }

    @RequestMapping(DASHBOARD_URL)
    fun dashboard(@RequestParam(name = DATE_ATTRIBUTE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) requestParamStartOfMonth: LocalDate?, @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): Any {
        requestParamStartOfMonth?.let{
            model.addAttribute(DATE_ATTRIBUTE, requestParamStartOfMonth)
        }
        val startOfMonth = model.getAttribute(DATE_ATTRIBUTE) as LocalDate
        val endOfMonth = endOfMonth(startOfMonth)
        model.addAttribute(START_OF_MONTH_DATE_ATTRIBUTE, convertToDate(startOfMonth))
        model.addAttribute(END_OF_MONTH_DATE_ATTRIBUTE, convertToDate(endOfMonth))

        val countAndSummaries = moveService.moveTypeSummaries(supplier, startOfMonth)
        val journeysSummary = moveService.journeysSummary(supplier, startOfMonth)

        model.addAttribute("months", MonthsWidget(convertToDate(startOfMonth), nextMonth = convertToDate(startOfMonth.plusMonths(1)), previousMonth = convertToDate(startOfMonth.minusMonths(1))))
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
    fun jumpToMonth(@Valid @ModelAttribute("form") form: JumpToMonthForm, result: BindingResult, model: ModelMap, ): Any {
        if (result.hasErrors()) {
            return "select-month"
        }

        model.addAttribute(DATE_ATTRIBUTE, MonthYearParser.atStartOf(form.date))

        return RedirectView(DASHBOARD_URL)
    }

    fun convertToDate(dateToConvert: LocalDate): Date {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant())
    }

    companion object{
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
    }
}
