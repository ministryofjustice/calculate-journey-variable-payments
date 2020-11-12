package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

data class Report(
        val move: ReportMove,
        val person: Person?,
        val moveEvents: List<Event> = listOf(),
        val journeysWithEvents: List<ReportJourneyWithEvents> = listOf()) {

    fun hasAllOf(vararg ets: EventType) = getEvents(*ets).size == ets.size
    fun hasAnyOf(vararg ets: EventType) = getEvents(*ets).isNotEmpty()
    fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

    fun getEvents(vararg ets: EventType) =
            this.moveEvents.filter { ets.map{it.value}.contains(it.type) } +
            this.journeysWithEvents.flatMap { it.events }.filter{ ets.map{it.value}.contains(it.type)  }

}

data class ReportJourneyWithEvents(val reportJourney: ReportJourney, val events: List<Event> = listOf()){

}