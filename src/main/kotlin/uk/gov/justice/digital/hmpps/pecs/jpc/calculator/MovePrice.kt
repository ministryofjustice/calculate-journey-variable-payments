package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveReportFilterer
import java.lang.RuntimeException

enum class MovePriceType(val filterer: (p: FilterParams, m: Collection<Report>) -> Sequence<Report>){
    STANDARD(MoveReportFilterer::standardMoveReports),
    LONG_HAUL(MoveReportFilterer::longHaulReports),
    REDIRECTION(MoveReportFilterer::redirectionReports),
    LOCKOUT(MoveReportFilterer::lockoutReports),
    MULTI(MoveReportFilterer::multiTypeReports)
}

data class PriceSummary(
        val percentage: Double = 0.0,
        val volume: Int = 0,
        val volumeUnpriced: Int = 0,
        val totalPriceInPence: Int = 0) {
    val totalPriceInPounds = totalPriceInPence.toDouble() / 100
}

data class MovePrices(
        val movePriceType: MovePriceType,
        val prices: List<MovePrice>,
        val summary: PriceSummary)

data class MovePrice(
        val report: Report,
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


fun List<MovePrices>.summary(): PriceSummary {
    return PriceSummary(
            sumByDouble { it.summary.percentage },
            sumBy { it.summary.volume },
            sumBy { it.summary.volumeUnpriced },
            sumBy { it.summary.totalPriceInPence }
    )
}

fun List<MovePrices>.withType(movePriceType: MovePriceType) =
        find { it.movePriceType == movePriceType } ?: MovePrices(movePriceType, listOf(), PriceSummary())

