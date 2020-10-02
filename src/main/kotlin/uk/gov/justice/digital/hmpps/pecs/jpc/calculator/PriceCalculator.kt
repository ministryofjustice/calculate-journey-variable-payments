package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveFilterer
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveFiltererParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovePersonJourneysEvents
import java.time.LocalDate

@Component
class PriceCalculatorFactory(
        @Autowired val locationRepository: LocationRepository,
        @Autowired val priceRepository: PriceRepository) {

    fun calculator(moves: List<MovePersonJourneysEvents>): PriceCalculator {
        val nomisAgencyId2Location = locationRepository.findAll().associateBy(Location::nomisAgencyId)
        val (sercoPrices, geoPrices) = priceRepository.findAll().partition { it.supplier == Supplier.SERCO }
        val sercoJourney2price = sercoPrices.associateBy { priceKey(it) }
        val geoJourney2price = geoPrices.associateBy { priceKey(it) }

        return PriceCalculator(nomisAgencyId2Location, sercoJourney2price, geoJourney2price, moves)
    }
}

class PriceCalculator(val nomisAgencyId2Location: Map<String, Location>,
                      val sercoJourney2price: Map<String, Price>,
                      val geoJourney2price: Map<String, Price>,
                      val allMoves: List<MovePersonJourneysEvents>) {

    fun priceKey(journey: Journey) =
            "${nomisAgencyId2Location[journey.fromLocation]?.siteName}-${nomisAgencyId2Location[journey.toLocation]?.siteName}"

    fun standardPrices(params: MoveFiltererParams): Sequence<MovePrice> {
        val standardMoves = MoveFilterer.standardMoves(params, allMoves)
        return standardMoves.map {
            val journeyPrice = when (params.supplier) {
                Supplier.SERCO -> sercoJourney2price[priceKey(it.journeysWithEvents[0].journey)]
                Supplier.GEOAMEY -> geoJourney2price[priceKey(it.journeysWithEvents[0].journey)]
            }

            val fromLocationType = nomisAgencyId2Location.get(it.move.fromLocation)?.locationType
            val toLocationType = nomisAgencyId2Location.get(it.move.toLocation)?.locationType

            MovePrice(fromLocationType, toLocationType, it, listOf(JourneyPrice(it.journeysWithEvents[0], journeyPrice?.priceInPence)))
        }
    }
}

fun priceKey(price: Price) = "${price.fromLocationName}-${price.toLocationName}"