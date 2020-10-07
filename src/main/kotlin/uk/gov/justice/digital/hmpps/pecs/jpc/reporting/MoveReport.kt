package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

data class MoveReport(
        val move: Move,
        val person: Person?,
        val events: List<Event> = listOf(),
        val journeysWithEvents: List<JourneyWithEvents> = listOf()) {

    fun hasAnyOf(vararg ets: EventType) =
            ets.sumBy { et ->
                this.events.count { it.hasType(et) } +
                this.journeysWithEvents.flatMap { it.events }.count { it.hasType(et) }
            } > 0

    fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

}

data class JourneyWithEvents(val journey: Journey, val events: List<Event> = listOf()){

}