package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType

data class Report(
        val move: ReportMove,
        val moveEvents: List<Event> = listOf(),
        val journeysWithEvents: List<JourneyWithEvents> = listOf()
        ) {

    /**
     * Returns the nullable MoveType for the Report
     * This goes through each filterer in turn to see if it is that MoveType
     * If it doesn't find any matching MoveType, return null
     */
    fun moveType() = MoveType.values().firstOrNull { it.filterer(listOf(this)).toList().isNotEmpty() }

    fun hasAllOf(vararg ets: EventType) = getEvents(*ets).size == ets.size
    fun hasAnyOf(vararg ets: EventType) = getEvents(*ets).isNotEmpty()
    fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

    fun getEvents(vararg ets: EventType) =
            this.moveEvents.filter { ets.map{it.value}.contains(it.type) } +
            this.journeysWithEvents.flatMap { it.events }.filter{ ets.map{it.value}.contains(it.type)  }

}

data class JourneyWithEvents(val reportJourney: ReportJourney, val events: List<Event> = listOf()){

}