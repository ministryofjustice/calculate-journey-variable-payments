package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MoneyTest {

  @Test
  internal fun `multiplier operator override for AdjustmentMultiplier`() {
    assertThat(Money.valueOf(10.00) * AdjustmentMultiplier(1.2.toBigDecimal())).isEqualTo(Money.valueOf(12.00))
    assertThat(Money.valueOf(10.00) * AdjustmentMultiplier(1.5.toBigDecimal())).isEqualTo(Money.valueOf(15.00))
    assertThat(Money.valueOf(12.00) * AdjustmentMultiplier(2.0.toBigDecimal())).isEqualTo(Money.valueOf(24.00))
  }

  @Test
  internal fun `double monetary values rounds down when rounding digit is below 5`() {
    assertThat(Money.valueOf(10.000).pence).isEqualTo(1000)
    assertThat(Money.valueOf(10.001).pence).isEqualTo(1000)
    assertThat(Money.valueOf(10.002).pence).isEqualTo(1000)
    assertThat(Money.valueOf(10.003).pence).isEqualTo(1000)
    assertThat(Money.valueOf(10.004).pence).isEqualTo(1000)
  }

  @Test
  internal fun `double monetary values rounds up when rounding digit is 5 or more`() {
    assertThat(Money.valueOf(10.005).pence).isEqualTo(1001)
    assertThat(Money.valueOf(10.006).pence).isEqualTo(1001)
    assertThat(Money.valueOf(10.007).pence).isEqualTo(1001)
    assertThat(Money.valueOf(10.008).pence).isEqualTo(1001)
    assertThat(Money.valueOf(10.009).pence).isEqualTo(1001)
    assertThat(Money.valueOf(10.999).pence).isEqualTo(1100)
  }
}
