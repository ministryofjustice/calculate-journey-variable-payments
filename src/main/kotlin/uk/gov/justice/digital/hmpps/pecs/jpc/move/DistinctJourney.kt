package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

data class DistinctJourney(
        val fromNomisAgencyId: String,
        val fromLocationType: LocationType?,
        val fromSiteName: String?,
        val toNomisAgencyId: String,
        val toLocationType: LocationType?,
        val toSiteName: String?
) {
    fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
    fun toSiteName() = toSiteName ?: toNomisAgencyId
}