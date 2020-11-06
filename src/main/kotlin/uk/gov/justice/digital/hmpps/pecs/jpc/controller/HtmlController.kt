package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.constraint.ValidMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MovesForMonthService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import javax.servlet.http.HttpSession
import javax.validation.Valid


data class MonthsWidget(val currentMonth: Date, val nextMonth: Date, val previousMonth: Date)

@Controller
class HtmlController(@Autowired val movesForMonthService: MovesForMonthService) {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView("/dashboard")
    }

    @RequestMapping("/choose-supplier")
    fun chooseSupplier(model: ModelMap): String {
        return "choose-supplier"
    }

    @RequestMapping("/choose-supplier/serco")
    fun chooseSupplierSerco(session: HttpSession): RedirectView {
        session.setAttribute("supplier", Supplier.SERCO)
        return RedirectView("/dashboard")
    }

    @RequestMapping("/choose-supplier/geoamey")
    fun chooseSupplierGeoAmey(session: HttpSession): RedirectView {
        session.setAttribute("supplier", Supplier.GEOAMEY)
        return RedirectView("/dashboard")
    }

    private fun addStartAndEndDatesToModel(localDate: LocalDate?, model: ModelMap): LocalDate{
        val startOfMonth = (localDate ?: LocalDate.now()).withDayOfMonth(1)
        val endOfMonth = endOfMonth(startOfMonth)
        model.addAttribute("startOfMonthDate", convertToDate(startOfMonth))
        model.addAttribute("endOfMonthDate", convertToDate(endOfMonth))
        return startOfMonth
    }

    @RequestMapping("/dashboard")
    fun dashboard(@SessionAttribute(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) localDate: LocalDate?, @SessionAttribute(name = "supplier") supplier: Supplier, model: ModelMap): Any {
        val startOfMonth = addStartAndEndDatesToModel(localDate, model)
        val countAndSummaries = movesForMonthService.moveTypeSummaries(supplier, startOfMonth)
        val uniqueJourneys = movesForMonthService.journeysSummary(supplier, startOfMonth)

        model.addAttribute("supplier", supplier)
        model.addAttribute("months", MonthsWidget(convertToDate(startOfMonth), nextMonth = convertToDate(startOfMonth.plusMonths(1)), previousMonth = convertToDate(startOfMonth.minusMonths(1))))
        model.addAttribute("summary", countAndSummaries.summary())
        model.addAttribute("uniqueJourneys", uniqueJourneys)
        model.addAttribute("summaries", countAndSummaries.allSummaries())
        return "dashboard"
    }

    @RequestMapping("/select-month", method = [RequestMethod.GET])
    fun selectMonth(@SessionAttribute(name = "supplier") supplier: Supplier, model: ModelMap): Any {
        model.addAttribute("supplier", supplier)
        model.addAttribute("form", JumpToMonthForm(date = ""))
        return "select-month"
    }

    data class JumpToMonthForm(@ValidMonthYear val date: String)

    @RequestMapping(value = ["/select-month"], method = [RequestMethod.POST])
    fun jumpToMonth(
            @Valid @ModelAttribute("form") form: JumpToMonthForm,
            result: BindingResult, model: ModelMap, session: HttpSession,
    ): Any {
        if (result.hasErrors()) {
            return "select-month"
        }
        val date = PrettyTimeParser().parse(form.date)[0]
        val localDate = convertToLocalDate(date)
        session.setAttribute("date", localDate)
        return RedirectView("/dashboard")
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
