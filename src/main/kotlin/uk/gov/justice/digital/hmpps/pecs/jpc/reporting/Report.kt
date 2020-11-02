package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

data class Report(
        val move: Move,
        val person: Person?,
        val events: List<Event> = listOf(),
        val journeysWithEvents: List<JourneyWithEvents> = listOf()) {

    fun hasAllOf(vararg ets: EventType) = getEvents(*ets).size == ets.size
    fun hasAnyOf(vararg ets: EventType) = getEvents(*ets).isNotEmpty()
    fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

    fun getEvents(vararg ets: EventType) =
            this.events.filter { ets.map{it.value}.contains(it.type) } +
            this.journeysWithEvents.flatMap { it.events }.filter{ ets.map{it.value}.contains(it.type)  }

}

data class JourneyWithEvents(val journey: Journey, val events: List<Event> = listOf()){

}