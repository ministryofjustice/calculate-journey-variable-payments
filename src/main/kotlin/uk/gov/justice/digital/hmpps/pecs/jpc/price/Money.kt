package uk.gov.justice.digital.hmpps.pecs.jpc.price

import java.lang.IllegalArgumentException

/**
 * Simple value object to encapsulate a monetary amount in pence. Negative amounts are not allowed.
 */
data class Money(val pence: Int) {

  init {
    if (pence < 0) throw IllegalArgumentException("money cannot be less than zero")
  }

  fun pounds() = pence.toDouble() / 100

  companion object Factory {
    fun valueOf(pounds: Double) = Money((pounds * 100).toInt())
  }
}
