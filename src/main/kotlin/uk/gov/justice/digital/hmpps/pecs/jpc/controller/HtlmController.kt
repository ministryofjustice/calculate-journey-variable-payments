package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.joda.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

@Controller
class HtmlController {

    @RequestMapping("/")
    fun homepage(model: ModelMap): RedirectView {
        return RedirectView("/dashboard")
    }

    @RequestMapping("/dashboard")
    fun dashboard(@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: java.time.LocalDate?, model: ModelMap): String {
        model.addAttribute("currentDate", LocalDate.now())
        return "dashboard"
    }
}