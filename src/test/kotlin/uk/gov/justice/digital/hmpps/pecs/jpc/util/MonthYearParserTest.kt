package uk.gov.justice.digital.hmpps.pecs.jpc.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class MonthYearParserTest {

  @Test
  internal fun `parse using format month digit and yyyy`() {
    assertThat(MonthYearParser.atStartOf("6/2020")).isEqualTo(LocalDate.of(2020, 6, 1))
    assertThat(MonthYearParser.atStartOf("06/2020")).isEqualTo(LocalDate.of(2020, 6, 1))
    assertThat(MonthYearParser.atStartOf("10/2021")).isEqualTo(LocalDate.of(2021, 10, 1))
    assertThat(MonthYearParser.atStartOf(" 10/2021")).isEqualTo(LocalDate.of(2021, 10, 1))
    assertThat(MonthYearParser.atStartOf("10/2021 ")).isEqualTo(LocalDate.of(2021, 10, 1))
    assertThat(MonthYearParser.atStartOf(" 10/2021 ")).isEqualTo(LocalDate.of(2021, 10, 1))
    assertThat(MonthYearParser.atStartOf("13/2022")).isNull()
  }

  @Test
  internal fun `parse using format - first 3 letter month and YYYY`() {
    assertThat(MonthYearParser.atStartOf("jan 2019")).isEqualTo(LocalDate.of(2019, 1, 1))
    assertThat(MonthYearParser.atStartOf(" jan 2019 ")).isEqualTo(LocalDate.of(2019, 1, 1))
    assertThat(MonthYearParser.atStartOf("fEb 2020")).isEqualTo(LocalDate.of(2020, 2, 1))
    assertThat(MonthYearParser.atStartOf("jAn 2021")).isEqualTo(LocalDate.of(2021, 1, 1))
    assertThat(MonthYearParser.atStartOf("jAnu 2021")).isNull()
  }

  @Test
  internal fun `parse using format - whole month in letters and yyyy`() {
    assertThat(MonthYearParser.atStartOf("october 2019")).isEqualTo(LocalDate.of(2019, 10, 1))
    assertThat(MonthYearParser.atStartOf(" october 2019 ")).isEqualTo(LocalDate.of(2019, 10, 1))
    assertThat(MonthYearParser.atStartOf("oCtober 2020")).isEqualTo(LocalDate.of(2020, 10, 1))
    assertThat(MonthYearParser.atStartOf("November 2021")).isEqualTo(LocalDate.of(2021, 11, 1))
  }

  @Test
  internal fun `is valid`() {
    assertThat(MonthYearParser.isValid("9/2020")).isTrue
    assertThat(MonthYearParser.isValid("q/2020")).isFalse
    assertThat(MonthYearParser.isValid("Oct 2019")).isTrue
    assertThat(MonthYearParser.isValid("0ct 2019")).isFalse
    assertThat(MonthYearParser.isValid("October 2019")).isTrue
    assertThat(MonthYearParser.isValid("0ctober 2019")).isFalse
  }
}
