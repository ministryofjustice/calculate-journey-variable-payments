package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Simple value object to encapsulate a monetary amount in pence. Zero or less are not allowed.
 */
data class Money(val pence: Int) {

  init {
    if (pence < 0) throw IllegalArgumentException("money cannot be less than zero")
  }

  fun pounds(): BigDecimal = pence.toBigDecimal().divide(BigDecimal("100")).setScale(2)

  override fun toString(): String = "%.2f".format(pounds())

  operator fun times(multiplier: BigDecimal) = valueOf(pounds().multiply(multiplier))

  companion object {

    /**
     * This half up, e.g. 10.004 would be rounded to 1000 pence and 10.005 would be rounded to 1001 pence.
     */
    fun valueOf(pounds: String) = valueOf(pounds.toBigDecimal())

    /**
     * This half up, e.g. 10.004 would be rounded to 1000 pence and 10.005 would be rounded to 1001 pence.
     */
    fun valueOf(pounds: BigDecimal) = Money(
      pounds
        .setScale(2, RoundingMode.HALF_UP)
        .multiply(BigDecimal("100"))
        .toInt()
    )
  }
}
