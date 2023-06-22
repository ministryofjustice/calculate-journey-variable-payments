package uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

data class JourneysSummary(
  val count: Int,
  val totalPriceInPence: Int,
  val countWithoutLocations: Int,
  val countUnpriced: Int,
  val supplier: Supplier,
)
