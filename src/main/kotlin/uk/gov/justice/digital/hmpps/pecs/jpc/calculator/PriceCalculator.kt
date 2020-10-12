package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReportFilterer
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Report
import java.lang.RuntimeException

@Component
class PriceCalculator(@Autowired val priceRepository: PriceRepository) {

    fun priceKey(journey: Journey) = "${journey.fromLocation.siteName}-${journey.toLocation.siteName}"

    fun allPrices(params: FilterParams, moves: List<Report>): List<MovePrices>{

        val journeysToPrice = priceRepository.findAllBySupplier(params.supplier).associateBy { it.journey }
        val completedMoves = MoveReportFilterer.completedMoves(params, moves).toList()

        return MovePriceType.values().map {
            val prices = filterThenPrice(params, moves, journeysToPrice, it.filterer).toList()
            val percentage: Double = if(completedMoves.isEmpty()) 0.0 else prices.size.toDouble() / completedMoves.size
            val volume = prices.size
            val volumeUnpriced = prices.count {it.totalInPence() == null}
            val totalPrice = prices.sumBy { it.totalInPence() ?: 0 }

            MovePrices(it, prices, PriceSummary(percentage, volume, volumeUnpriced, totalPrice))
        }
    }

    private fun filterThenPrice(
            params: FilterParams,
            moves: List<Report>,
            journeysToPrice: Map<String, Price>,
            f: (p: FilterParams, m: Collection<Report>) -> Sequence<Report>): Sequence<MovePrice>{
        return f(params, moves).map {
            val journeyPrices = it.journeysWithEvents.map {
                JourneyPrice(it,
                        if(it.journey.billable) {
                                journeysToPrice[priceKey(it.journey)]?.priceInPence
                        } else null
                )
            }
            MovePrice(it, journeyPrices)
        }
    }
}

