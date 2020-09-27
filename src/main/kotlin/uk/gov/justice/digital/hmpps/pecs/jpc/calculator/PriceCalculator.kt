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
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovePersonJourneysEvents

@Component
class PriceCalculator(
        @Autowired val locationRepository: LocationRepository,
        @Autowired val priceRepository: PriceRepository) {

    lateinit var nomisAgencyId2Location: Map<String, Location>
    lateinit var sercoJourney2price: Map<String, Price>
    lateinit var geoJourney2price: Map<String, Price>


    init {
        nomisAgencyId2Location = locationRepository.findAll().associateBy(Location::nomisAgencyId)
        val (sercoPrices, geoPrices) = priceRepository.findAll().partition { it.supplier == Supplier.SERCO }
        sercoJourney2price = sercoPrices.associateBy { priceKey(it) }
        geoJourney2price = geoPrices.associateBy { priceKey(it) }

    }

    private fun priceKey(price: Price) = "${price.fromLocationName}-${price.toLocationName}"
    private fun priceKey(journey: Journey) =
            "${nomisAgencyId2Location[journey.fromLocation]?.siteName}-${nomisAgencyId2Location[journey.toLocation]?.siteName}"


    fun standardPrices(supplier: Supplier, allMoves: List<MovePersonJourneysEvents>): List<MovePrice> {
        val standardMoves = MoveFilterer.standardMoves(supplier, allMoves)
        return standardMoves.map {
            val journeyPrice = sercoJourney2price[priceKey(it.journeysWithEvents[0].journey)]?.priceInPence
            MovePrice(it, listOf(JourneyPrice(it.journeysWithEvents[0], journeyPrice)))
        }
    }
}