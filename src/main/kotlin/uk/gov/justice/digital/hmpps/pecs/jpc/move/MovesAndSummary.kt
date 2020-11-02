package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

data class Summary(
        val percentage: Double = 0.0,
        val volume: Int = 0,
        val volumeUnpriced: Int = 0,
        val totalPriceInPence: Int = 0) {
    val totalPriceInPounds = totalPriceInPence.toDouble() / 100
}

data class MovesAndSummary(
        val moves: List<Move>,
        val summary: Summary
)

data class MoveTypeWithMovesAndSummary(
        val standard: MovesAndSummary,
        val longHaul: MovesAndSummary,
        val redirection: MovesAndSummary,
        val lockout: MovesAndSummary,
        val multi: MovesAndSummary,
        val cancelled: MovesAndSummary
) {
    fun summary(): Summary {
        return with(allByMoveType().values) {
            Summary(
                    sumByDouble { it.summary.percentage },
                    sumBy { it.summary.volume },
                    sumBy { it.summary.volumeUnpriced },
                    sumBy { it.summary.totalPriceInPence }
            )
        }
    }

    fun allByMoveType() = mapOf(
            MoveType.STANDARD to standard,
            MoveType.LONG_HAUL to longHaul,
            MoveType.REDIRECTION to redirection,
            MoveType.LOCKOUT to lockout,
            MoveType.MULTI to multi,
            MoveType.CANCELLED to cancelled
    )

    fun summariesByMoveType() = allByMoveType().map { (k, v) -> (k to v.summary) }.toMap()

    val uniqueJourneys = UniqueJourneys(
            allByMoveType().flatMap { it.value.moves }.distinctBy { "${it.fromNomisAgencyId}-${it.toNomisAgencyId}" }.map {
                UniqueJourney(it.fromNomisAgencyId, it.fromSiteName, it.fromLocationType, it.toNomisAgencyId, it.toSiteName, it.toLocationType,
                        it.fromSiteName != null && it.toSiteName != null, it.hasPrice())
            })

}

data class UniqueJourneys(val journeys: List<UniqueJourney>) {
    fun countUnpriced() = journeys.count { !it.priced }
    fun countUnLocationed() = journeys.count { !it.locationed }
    fun count() = journeys.size
}


data class UniqueJourney(
        val fromNomisAgencyId: String,
        val fromSiteName: String? = null,
        val fromLocationType: LocationType? = null,
        val toNomisAgencyId: String?,
        val toSiteName: String? = null,
        val toLocationType: LocationType? = null,
        val locationed: Boolean,
        val priced: Boolean
)