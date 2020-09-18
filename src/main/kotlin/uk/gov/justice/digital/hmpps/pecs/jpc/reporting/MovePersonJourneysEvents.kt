package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

class MovePersonJourneysEvents(val move: Move, val person: Person?, val events: List<Event> = listOf(), val journeysWithEvents: List<JourneyWithEvents> = listOf()) {

}

class JourneyWithEvents(val journey: Journey, val events: List<Event> = listOf()){

}