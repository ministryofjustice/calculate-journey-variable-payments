package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class EffectiveYearTest {

  @Test
  fun `current effective year 2019`() {
    assertThat(EffectiveYear { september(2019) }.current()).isEqualTo(2019)
    assertThat(EffectiveYear { august(2020) }.current()).isEqualTo(2019)
  }

  @Test
  fun `current effective year 2020`() {
    assertThat(EffectiveYear { september(2020) }.current()).isEqualTo(2020)
    assertThat(EffectiveYear { august(2021) }.current()).isEqualTo(2020)
  }

  @Test
  fun `current effective year 2021`() {
    assertThat(EffectiveYear { september(2021) }.current()).isEqualTo(2021)
    assertThat(EffectiveYear { august(2022) }.current()).isEqualTo(2021)
  }

  @Test
  fun `effective year for september 2019 date`() {
    assertThat(effectiveYearForDate(september(2019).toLocalDate())).isEqualTo(2019)
  }

  @Test
  fun `effective year for august 2020 date`() {
    assertThat(effectiveYearForDate(august(2020).toLocalDate())).isEqualTo(2019)
  }

  @Test
  fun `next effective year for september 2019 date`() {
    assertThat(nextEffectiveYearForDate(september(2019).toLocalDate())).isEqualTo(2020)
  }

  @Test
  fun `next effective year for august 2020 date`() {
    assertThat(nextEffectiveYearForDate(august(2020).toLocalDate())).isEqualTo(2020)
  }

  private fun september(year: Int) = LocalDateTime.of(year, 9, 1, 0, 0)

  private fun august(year: Int) = LocalDateTime.of(year, 8, 31, 0, 0)
}
