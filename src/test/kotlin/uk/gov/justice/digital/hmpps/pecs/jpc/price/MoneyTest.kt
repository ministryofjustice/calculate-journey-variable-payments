package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MoneyTest {
  @Test
  internal fun `multiplier override`() {
    assertThat(Money.valueOf(10.00) * 1.2).isEqualTo(Money.valueOf(12.00))
    assertThat(Money.valueOf(10.00) * 1.5).isEqualTo(Money.valueOf(15.00))
    assertThat(Money.valueOf(12.00) * 2.0).isEqualTo(Money.valueOf(24.00))
  }
}
