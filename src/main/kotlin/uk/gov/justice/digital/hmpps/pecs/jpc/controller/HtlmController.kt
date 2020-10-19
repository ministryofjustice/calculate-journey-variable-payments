package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

data class Move(val type: String, val percentage: Double, val withoutPrices: Int, val total: Int, val pendingPrice: Double)
data class Summary(val month: Date, val movesWithoutPrices: Int, val totalMoves: Int, val supplier: String, val jpcVersion: String, val totalPrice: Double)
data class JourneySummary(val movesWithoutPrices: Int, val movesWithoutLocations: Int, val totalUniqueJourneys: Int)

@Controller
class HtmlController {

    @RequestMapping("/")
    fun homepage(model: ModelMap): String {
        model["title"] = "Calculate Journey Variable Payments"
        return "index"
    }

    @RequestMapping("/dashboard")
    fun dashboard(model: ModelMap): String {
        model.addAttribute("summary", Summary(month = Date(), movesWithoutPrices = 1, totalMoves = 100, supplier = "SERCO", jpcVersion = "JPC_SERCO_310320", totalPrice = 100000.0))
        model.addAttribute("moves", listOf(Move("#1", 95.0, 2, 1000, 1000.0)))
        model.addAttribute("journeySummary", JourneySummary(movesWithoutPrices = 12, movesWithoutLocations = 24, totalUniqueJourneys = 48))
        return "dashboard"
    }
}