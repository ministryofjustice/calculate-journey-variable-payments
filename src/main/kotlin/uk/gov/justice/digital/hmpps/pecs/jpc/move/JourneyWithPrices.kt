package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

data class JourneyWithPrices(
        val fromNomisAgencyId: String,
        val fromLocationType: LocationType?,
        val fromSiteName: String?,
        val toNomisAgencyId: String,
        val toLocationType: LocationType?,
        val toSiteName: String?,
        val volume: Int,
        val unitPriceInPence: Int?,
        val totalPriceInPence: Int?
) {
    fun unitPriceInPounds() = unitPriceInPence?.let{it.toDouble() / 100}
    fun totalPriceInPounds() = totalPriceInPence?.let{it.toDouble() / 100}
    fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
    fun toSiteName() = toSiteName ?: toNomisAgencyId
    }