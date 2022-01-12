package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.math.BigDecimal
import kotlin.math.roundToInt

/**
 * Simple value object to encapsulate a monetary amount in pence. Negative amounts are not allowed.
 */
data class Money(val pence: Int) {

  init {
    if (pence < 0) throw IllegalArgumentException("money cannot be less than zero")
  }

  fun pounds() = pence.toDouble() / 100

  override fun toString(): String = "%.2f".format(pounds())

  operator fun times(multiplier: BigDecimal) = valueOf(pounds().toBigDecimal().times(multiplier).toDouble())

  companion object Factory {
    /**
     * This rounds the amount towards positive infinity, e.g. 10.004 would be rounded to 1000 pence and 10.005 would be
     * rounded to 1001 pence.
     */
    fun valueOf(pounds: Double) = Money((pounds * 100).roundToInt())
  }
}
