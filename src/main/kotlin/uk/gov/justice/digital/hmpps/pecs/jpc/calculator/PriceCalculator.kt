package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*

@Component
class PriceCalculator(@Autowired val priceRepository: PriceRepository) {

    fun priceKey(journey: Journey) = "${journey.fromLocation}-${journey.toLocation}"

    fun allPrices(params: FilterParams, moves: List<Report>): List<MovePrices> {

        val journeysToPrice = priceRepository.findAllBySupplier(params.supplier).associateBy { it.journey() }

        val completedMoves = ReportFilterer.completedMoves(params, moves).toList()
        val cancelledBillableMoves = ReportFilterer.cancelledBillableMoves(params, moves).toList()
        val completedAndCancelledMoves = completedMoves + cancelledBillableMoves

        return MovePriceType.values().map {
            val prices = filterThenPrice(it, params, completedAndCancelledMoves, journeysToPrice).toList()
            val percentage: Double = if (completedAndCancelledMoves.isEmpty()) 0.0 else prices.size.toDouble() / completedAndCancelledMoves.size
            val volume = prices.size
            val volumeUnpriced = prices.count { it.totalInPence() == null }
            val totalPrice = prices.sumBy { it.totalInPence() ?: 0 }

            MovePrices(it, prices, PriceSummary(percentage, volume, volumeUnpriced, totalPrice))
        }
    }

    private fun filterThenPrice(
            movePriceType: MovePriceType,
            params: FilterParams,
            moves: List<Report>,
            journeysToPrice: Map<String, Price>
    ): Sequence<MovePrice> {
        return movePriceType.filterer(params, moves).map {
            val journeyPrices = it.journeysWithEvents.map {
                JourneyPrice(it,
                    if (it.journey.billable) {
                        journeysToPrice[priceKey(it.journey)]?.priceInPence
                    } else null
                )
            }
            MovePrice(it, journeyPrices)
        }
    }
}

