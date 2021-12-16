package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.math.BigDecimal

/**
 * The multiplier value used for price adjustments. This allows validation logic/rules to be easily applied if needed.
 */
class AdjustmentMultiplier(val value: BigDecimal) {
  companion object {
    fun valueOf(value: String) = AdjustmentMultiplier(value.toBigDecimal())
  }
}
