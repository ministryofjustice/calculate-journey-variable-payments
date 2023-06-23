package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money

data class MovesSummary(
  val moveType: MoveType? = null,
  val percentage: Double = 0.0,
  val volume: Int = 0,
  val volumeUnpriced: Int = 0,
  val totalPriceInPence: Int = 0,
) {
  val totalPriceInPounds = Money(totalPriceInPence).pounds()
}
