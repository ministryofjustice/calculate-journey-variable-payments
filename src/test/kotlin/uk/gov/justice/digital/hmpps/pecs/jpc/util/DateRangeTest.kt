package uk.gov.justice.digital.hmpps.pecs.jpc.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.Year

class DateRangeTest {

  @Test
  fun `listOf for Jan 2021`() {
    val range = DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31))

    assertThat(range.listOf()).containsExactly(
      range(Month.JANUARY, Year.of(2021)),
    )
  }

  @Test
  fun `listOf Jan 2021 to Feb 2021`() {
    val range = DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 1))

    assertThat(range.listOf()).containsExactly(
      range(Month.JANUARY, Year.of(2021)),
      DateRange(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 2, 1)),
    )
  }

  @Test
  fun `listOf for entire year for 2021`() {
    val range = DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31))

    assertThat(range.listOf()).containsExactly(
      range(Month.JANUARY, Year.of(2021)),
      range(Month.FEBRUARY, Year.of(2021)),
      range(Month.MARCH, Year.of(2021)),
      range(Month.APRIL, Year.of(2021)),
      range(Month.MAY, Year.of(2021)),
      range(Month.JUNE, Year.of(2021)),
      range(Month.JULY, Year.of(2021)),
      range(Month.AUGUST, Year.of(2021)),
      range(Month.SEPTEMBER, Year.of(2021)),
      range(Month.OCTOBER, Year.of(2021)),
      range(Month.NOVEMBER, Year.of(2021)),
      range(Month.DECEMBER, Year.of(2021)),
    )
  }

  @Test
  fun `listOf when crossing year boundaries 2021 and 2022`() {
    val range = DateRange(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 31))

    assertThat(range.listOf()).containsExactly(
      range(Month.DECEMBER, Year.of(2021)),
      range(Month.JANUARY, Year.of(2022)),
    )
  }

  private fun range(month: Month, year: Year) = LocalDate.of(year.value, month.value, 1).let { DateRange(it, it.plusMonths(1).minusDays(1)) }
}
