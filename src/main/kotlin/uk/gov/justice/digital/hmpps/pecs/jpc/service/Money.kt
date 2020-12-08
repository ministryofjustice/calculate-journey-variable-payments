package uk.gov.justice.digital.hmpps.pecs.jpc.service

/**
 * Simple value object to encapsulate a monetary amount in pence.
 */
data class Money(val pence: Int) {
  fun pounds() = pence.toDouble() / 100

  companion object Factory {
    fun valueOf(pounds: Double) = Money((pounds * 100).toInt())
  }
}
