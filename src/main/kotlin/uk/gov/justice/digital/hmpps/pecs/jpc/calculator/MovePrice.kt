package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovePersonJourneysEvents

data class MovePrice(
        val fromLocationType: LocationType?,
        val toLocationType: LocationType?,
        val movePersonJourneysEvents: MovePersonJourneysEvents,
        val journeyPrices: List<JourneyPrice>
){
    /**
     * Returns the price in pence if all billable journeys have a price
     * Otherwise returns null
     */
    fun totalInPence(): Int? {
        return if(journeyPrices.any{ it.priceInPence == null }) null
        else journeyPrices.filter {  it.journeyWithEvents.journey.billable }.sumBy { it.priceInPence!! }
    }
}

data class JourneyPrice(
        val journeyWithEvents: JourneyWithEvents,
        val priceInPence: Int?)