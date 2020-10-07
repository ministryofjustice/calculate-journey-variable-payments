package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReportFilterer
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReport

@Component
class PriceCalculatorFactory(@Autowired val priceRepository: PriceRepository) {

    fun calculator(moves: List<MoveReport>): PriceCalculator {
        val (sercoPrices, geoPrices) = priceRepository.findAll().partition { it.supplier == Supplier.SERCO }
        val sercoJourney2price = sercoPrices.associateBy { priceKey(it) }
        val geoJourney2price = geoPrices.associateBy { priceKey(it) }

        return PriceCalculator(sercoJourney2price, geoJourney2price, moves)
    }
}

class PriceCalculator(val sercoJourney2price: Map<String, Price>,
                      val geoJourney2price: Map<String, Price>,
                      val moves: List<MoveReport>) {

    fun priceKey(journey: Journey) = "${journey.fromLocation.siteName}-${journey.toLocation.siteName}"

    fun standardPrices(params: FilterParams) = movePrices(params, MoveReportFilterer::standardMoveReports)

    fun redirectionPrices(params: FilterParams) = movePrices(params, MoveReportFilterer::redirectionReports)

    fun longHaulPrices(params: FilterParams) = movePrices(params, MoveReportFilterer::longHaulReports)

    private fun movePrices(params: FilterParams,
                           f: (p: FilterParams, m: Collection<MoveReport>) -> Sequence<MoveReport>): Sequence<MovePrice>{
        return f(params, moves).map {

            val journeyPrices = it.journeysWithEvents.map {
                JourneyPrice(it,
                        if(it.journey.billable) {
                            when (params.supplier) {
                                Supplier.SERCO -> sercoJourney2price[priceKey(it.journey)]
                                Supplier.GEOAMEY -> geoJourney2price[priceKey(it.journey)]
                            }?.priceInPence
                        } else null
                )
            }
            MovePrice(it, journeyPrices)
        }
    }
}

fun priceKey(price: Price) = "${price.fromLocationName}-${price.toLocationName}"