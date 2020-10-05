package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReportFilterer
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReport

@Component
class PriceCalculatorFactory(
        @Autowired val locationRepository: LocationRepository,
        @Autowired val priceRepository: PriceRepository) {

    fun calculator(moves: List<MoveReport>): PriceCalculator {
        val nomisAgencyId2Location = locationRepository.findAll().associateBy(Location::nomisAgencyId)
        val (sercoPrices, geoPrices) = priceRepository.findAll().partition { it.supplier == Supplier.SERCO }
        val sercoJourney2price = sercoPrices.associateBy { priceKey(it) }
        val geoJourney2price = geoPrices.associateBy { priceKey(it) }

        return PriceCalculator(nomisAgencyId2Location, sercoJourney2price, geoJourney2price, moves)
    }
}

class PriceCalculator(val agencyId2Location: Map<String, Location>,
                      val sercoJourney2price: Map<String, Price>,
                      val geoJourney2price: Map<String, Price>,
                      val moves: List<MoveReport>) {

    fun priceKey(journey: Journey) =
            "${agencyId2Location[journey.fromLocation]?.siteName}-${agencyId2Location[journey.toLocation]?.siteName}"

    fun standardPrices(params: FilterParams) = movePrices(params, MoveReportFilterer::standardMoveReports)

    fun redirectionPrices(params: FilterParams) = movePrices(params, MoveReportFilterer::redirectionReports)

    private fun movePrices(params: FilterParams,
                           f: (p: FilterParams, m: Collection<MoveReport>) -> Sequence<MoveReport>): Sequence<MovePrice>{
        return f(params, moves).map {
            val fromLocation = agencyId2Location.get(it.move.fromLocation)
            val toLocation = agencyId2Location.get(it.move.toLocation)

            val journeyPrices = it.journeysWithEvents.map {
                JourneyPrice(it,
                        when (params.supplier) {
                            Supplier.SERCO -> sercoJourney2price[priceKey(it.journey)]
                            Supplier.GEOAMEY -> geoJourney2price[priceKey(it.journey)]
                        }?.priceInPence
                )
            }
            MovePrice(fromLocation, toLocation, it, journeyPrices)
        }
    }
}

fun priceKey(price: Price) = "${price.fromLocationName}-${price.toLocationName}"