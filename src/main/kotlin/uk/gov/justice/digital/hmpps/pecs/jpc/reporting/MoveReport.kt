package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

data class MoveReport(
        val move: Move,
        val person: Person?,
        val events: List<Event> = listOf(),
        val journeysWithEvents: List<JourneyWithEvents> = listOf()) {

    fun hasEvent(et:EventType) =
            this.events.count { it.hasType(et) } +
            this.journeysWithEvents.flatMap { it.events }.count {it.hasType(et)} > 0
}

data class JourneyWithEvents(val journey: Journey, val events: List<Event> = listOf()){

}