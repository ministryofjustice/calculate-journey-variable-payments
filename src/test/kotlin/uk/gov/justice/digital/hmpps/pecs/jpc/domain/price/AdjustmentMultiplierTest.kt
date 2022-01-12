package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AdjustmentMultiplierTest {

  @Test
  fun `exception is thrown for zero multiplier`() {
    assertThatThrownBy { AdjustmentMultiplier(BigDecimal.ZERO) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Multiplier cannot be zero.")
  }

  @Test
  fun `times by money returns the expected monetary amount for percentage increases`() {
    assertThat(AdjustmentMultiplier.valueOf("0.67") * money("154.46")).isEqualTo(money("155.49"))
    assertThat(AdjustmentMultiplier.valueOf("20") * money("100.00")).isEqualTo(money("120.00"))
  }

  @Test
  fun `times by money returns the expected monetary amount for percentage decreases`() {
    assertThat(AdjustmentMultiplier.valueOf("-20") * money("100.00")).isEqualTo(money("80.00"))
    assertThat(AdjustmentMultiplier.valueOf("-12.01") * money("240.07")).isEqualTo(money("211.24"))
  }

  private fun money(amount: String) = Money.valueOf(amount)
}
