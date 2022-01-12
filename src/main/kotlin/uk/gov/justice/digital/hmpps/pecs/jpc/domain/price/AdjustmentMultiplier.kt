package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.math.BigDecimal

/**
 * The multiplier is a percentage value used for price adjustments. This allows validation logic/rules to be easily
 * applied if needed.
 */
data class AdjustmentMultiplier(val value: BigDecimal) {

  private val multiplier: BigDecimal = value.divide(BigDecimal(100)).plus(BigDecimal.ONE).also { println(it) }

  /**
   * Returns the amount plus the calculated adjustment multiplier.
   */
  operator fun times(amount: Money): Money = amount * multiplier

  companion object {
    fun valueOf(value: String) = AdjustmentMultiplier(BigDecimal(value))
  }
}
