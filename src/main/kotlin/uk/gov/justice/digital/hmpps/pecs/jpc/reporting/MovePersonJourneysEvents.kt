package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

data class MovePersonJourneysEvents(val move: Move, val person: Person?, val events: List<Event> = listOf(), val journeysWithEvents: List<JourneyWithEvents> = listOf()) {

}

data class JourneyWithEvents(val journey: Journey, val events: List<Event> = listOf()){

}