package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat.forPattern
import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidMonthYear
import java.util.*
import javax.validation.Valid

data class MonthsWidget(val currentMonth: Date, val nextMonth: Date, val previousMonth: Date)

@Controller
class HtmlController {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView("/dashboard")
    }

    @RequestMapping("/dashboard")
    fun dashboard(@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: java.time.LocalDate?, model: ModelMap): String {
        val currentDate = (if (date != null) LocalDate(date.year, date.monthValue, date.dayOfMonth) else LocalDate()).withDayOfMonth(1)
        model.addAttribute("currentDate", currentDate)
        model.addAttribute("months", MonthsWidget(currentDate.toDate(), nextMonth = LocalDate(currentDate).plusMonths(1).toDate(), previousMonth = LocalDate(currentDate).minusMonths(1).toDate()))
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
        val localDate = LocalDate(date)
        return RedirectView("/dashboard?date=${localDate.toString(forPattern("yyyy-MM-01"))}")
    }
}