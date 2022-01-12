package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AdjustmentMultiplierTest {

  @Test
  fun `times by money returns the monetary amount plus the percentage increase`() {
    assertThat(AdjustmentMultiplier.valueOf("0.67") * money(154.46)).isEqualTo(money(155.49))
    assertThat(AdjustmentMultiplier.valueOf("-20") * money(100.00)).isEqualTo(money(80.00))
    assertThat(AdjustmentMultiplier.valueOf("-12.01") * money(240.07)).isEqualTo(money(211.24))
  }

  private fun money(amount: Double) = Money.valueOf(amount)
}
