package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovePersonJourneysEvents

data class MovePrice(
        val movePersonJourneysEvents: MovePersonJourneysEvents,
        val journeyPrices: List<JourneyPrice>
){

}

data class JourneyPrice(
        val journey: JourneyWithEvents,
        val priceInPence: Int?
){

}