package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.ocpsoft.prettytime.nlp.PrettyTimeParser
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import javax.validation.Valid


data class MonthsWidget(val currentMonth: Date, val nextMonth: Date, val previousMonth: Date)
data class Summary(val date: Date, val movesWithoutPrices: Int, val totalMoves: Int, val supplier: String, val jpcVersion: String, val totalPrice: Double)
data class JourneySummary(val movesWithoutPrices: Int, val movesWithoutLocations: Int, val totalUniqueJourneys: Int)
data class Move(val type: String, val percentage: Double, val withoutPrices: Int, val total: Int, val pendingPrice: Double)

@Controller
class HtmlController {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView("/dashboard")
    }

    @RequestMapping("/dashboard")
    fun dashboard(@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: java.time.LocalDate?, model: ModelMap): String {
        val currentDate = (date ?: LocalDate.now()).withDayOfMonth(1)
        val supplierName = "SERCO"
        model.addAttribute("currentDate", convertToDate(currentDate))
        model.addAttribute("months", MonthsWidget(convertToDate(currentDate), nextMonth = convertToDate(currentDate.plusMonths(1)), previousMonth = convertToDate(currentDate.minusMonths(1))))
        model.addAttribute("summary", Summary(date = convertToDate(currentDate), movesWithoutPrices = 1, totalMoves = 100, supplier = supplierName, jpcVersion = "JPC_SERCO_310320", totalPrice = 100000.0))
        model.addAttribute("journeySummary", JourneySummary(movesWithoutPrices = 12, movesWithoutLocations = 24, totalUniqueJourneys = 48))
        model.addAttribute("moves", listOf(Move("#1", 95.0, 2, 1000, 1000.0)))
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
