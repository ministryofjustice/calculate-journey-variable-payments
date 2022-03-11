package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
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

  @Test
  fun `prices changes cannot be applied after the current year`() {
    assertThat(EffectiveYear { september(2020) }.canAddOrUpdatePrices(2021)).isFalse
  }

  @Test
  fun `prices changes can be applied in the current and previous year`() {
    assertThat(EffectiveYear { september(2020) }.canAddOrUpdatePrices(2020)).isTrue
    assertThat(EffectiveYear { september(2020) }.canAddOrUpdatePrices(2019)).isTrue
  }

  @Test
  fun `prices changes cannot be applied before the previous year`() {
    assertThat(EffectiveYear { september(2020) }.canAddOrUpdatePrices(2018)).isFalse
  }

  @Test
  fun `ordinal month and years are correct for effective year 2020`() {
    assertThat(ordinalMonthsAndYearForSeptemberToAugust(2020)).isEqualTo(
      listOf(
        9 to 2020,
        10 to 2020,
        11 to 2020,
        12 to 2020,
        1 to 2021,
        2 to 2021,
        3 to 2021,
        4 to 2021,
        5 to 2021,
        6 to 2021,
        7 to 2021,
        8 to 2021,
      )
    )
  }

  @Test
  fun `ordinal month and years are correct for effective year 2021`() {
    assertThat(ordinalMonthsAndYearForSeptemberToAugust(2021)).isEqualTo(
      listOf(
        9 to 2021,
        10 to 2021,
        11 to 2021,
        12 to 2021,
        1 to 2022,
        2 to 2022,
        3 to 2022,
        4 to 2022,
        5 to 2022,
        6 to 2022,
        7 to 2022,
        8 to 2022,
      )
    )
  }

  @Test
  fun `start of contract is Sep 1st 2020`() {
    assertThat(EffectiveYear.startOfContract()).isEqualTo(LocalDate.of(2020, 9, 1))
  }

  private fun september(year: Int) = LocalDateTime.of(year, 9, 1, 0, 0)

  private fun august(year: Int) = LocalDateTime.of(year, 8, 31, 0, 0)
}
