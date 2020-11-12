package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

data class UniqueJourney(
        val fromNomisAgencyId: String,
        val fromLocationType: LocationType?,
        val fromSiteName: String?,
        val toNomisAgencyId: String,
        val toLocationType: LocationType?,
        val toSiteName: String?,
        val volume: Int,
        val totalPriceInPence: Int?
) {
}