package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.DashboardService
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import javax.validation.Valid


data class MonthsWidget(val currentMonth: Date, val nextMonth: Date, val previousMonth: Date)

@Controller
class HtmlController(@Autowired val dashboardService: DashboardService) {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView("/dashboard")
    }

    @RequestMapping("/dashboard")
    fun dashboard(@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?, model: ModelMap): String {
        val startOfMonthDate = (date ?: LocalDate.now()).withDayOfMonth(1)
        val endOfMonthDate = startOfMonthDate.plusMonths(1).minusDays(1)
        val supplier = Supplier.SERCO // FIXME get from session
        model.addAttribute("startOfMonthDate", convertToDate(startOfMonthDate))
        model.addAttribute("endOfMonthDate", convertToDate(endOfMonthDate))
        model.addAttribute("supplier", supplier)
        val moves = dashboardService.movesForMonth(supplier, startOfMonthDate)

        model.addAttribute("months", MonthsWidget(convertToDate(startOfMonthDate), nextMonth = convertToDate(startOfMonthDate.plusMonths(1)), previousMonth = convertToDate(startOfMonthDate.minusMonths(1))))
        model.addAttribute("summary", moves.summary())
        model.addAttribute("uniqueJourneys", moves.uniqueJourneys)
        model.addAttribute("moveTypeToSummary", moves.summariesByMoveType())
        return "dashboard"
    }

    @RequestMapping("/select-month", method = [RequestMethod.GET])
    fun selectMonth(model: ModelMap): String {
        model.addAttribute("form", JumpToMonthForm(date = ""))
        return "select-month"
    }

    data class JumpToMonthForm(@ValidMonthYear val date: String)

    @RequestMapping(value = ["/select-month"], method = [RequestMethod.POST])
    fun jumpToMonth(
            @Valid @ModelAttribute("form") form: JumpToMonthForm,
            result: BindingResult, model: ModelMap,
    ): Any {
        if (result.hasErrors()) {
            return "select-month"
        }
        val date = PrettyTimeParser().parse(form.date)[0]
        val localDate = convertToLocalDate(date)
        return RedirectView("/dashboard?date=${localDate.format(ofPattern("yyyy-MM-01"))}")
    }

    fun convertToDate(dateToConvert: LocalDate): Date {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant())
    }

    fun convertToLocalDate(dateToConvert: Date): LocalDate {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
    }
}
