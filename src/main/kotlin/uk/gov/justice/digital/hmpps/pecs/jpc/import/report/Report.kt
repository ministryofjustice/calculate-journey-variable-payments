package uk.gov.justice.digital.hmpps.pecs.jpc.import.report

data class Report(
        val reportMove: ReportMove,
        val reportPerson: ReportPerson?,
        val reportEvents: List<ReportEvent> = listOf(),
        val journeysWithEventReports: List<ReportJourneyWithEvents> = listOf()) {

    fun hasAllOf(vararg ets: EventType) = getEvents(*ets).size == ets.size
    fun hasAnyOf(vararg ets: EventType) = getEvents(*ets).isNotEmpty()
    fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

    fun getEvents(vararg ets: EventType) =
            this.reportEvents.filter { ets.map{it.value}.contains(it.type) } +
            this.journeysWithEventReports.flatMap { it.reportEvents }.filter{ ets.map{it.value}.contains(it.type)  }

}

data class ReportJourneyWithEvents(val reportJourney: ReportJourney, val reportEvents: List<ReportEvent> = listOf()){

}