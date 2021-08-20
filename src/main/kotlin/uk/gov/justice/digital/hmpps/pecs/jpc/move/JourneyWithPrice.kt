package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money

data class JourneyWithPrice(
  val fromNomisAgencyId: String,
  val fromLocationType: LocationType?,
  val fromSiteName: String?,
  val toNomisAgencyId: String,
  val toLocationType: LocationType?,
  val toSiteName: String?,
  val volume: Int?,
  val unitPriceInPence: Int?,
  val totalPriceInPence: Int?
) {
  fun unitPriceInPounds() = unitPriceInPence?.let { Money(it).pounds() }
  fun totalPriceInPounds() = totalPriceInPence?.let { Money(it).pounds() }
  fun billableJourneyCount() = unitPriceInPence?.let { totalPriceInPence!! / it } ?: 0
  fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
  fun toSiteName() = toSiteName ?: toNomisAgencyId
}
