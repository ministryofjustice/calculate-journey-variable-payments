package uk.gov.justice.digital.hmpps.pecs.jpc.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/**
 * Simple month and year parser supporting the following formats:
 *
 * M/yyyy e.g. 9/2020,
 * MMM yyyy e.g Jan 2020
 * MMMM yyyy e.g. October 2020.
 *
 * Characters can also be in mixed case. Whitespace characters at the beginning or end are also allowed.
 */
object MonthYearParser {

  fun isValid(monthYear: String): Boolean {
    return atStartOf(monthYear) != null
  }

  /**
   * @return [LocalDate] at the beginning of the month for the given month and year if parsable, otherwise null.
   */
  fun atStartOf(monthYear: String): LocalDate? {
    return Result.runCatching {
      YearMonth.parse(monthYear.trim(), DateTimeFormatter.ofPattern("M/yyyy")).atDay(1)
    }.getOrElse {
      Result.runCatching {
        YearMonth.parse(monthYear.trim(), DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM yyyy").toFormatter()).atDay(1)
      }.getOrElse {
        Result.runCatching {
          YearMonth.parse(monthYear.trim(), DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM yyyy").toFormatter()).atDay(1)
        }.getOrNull()
      }
    }
  }
}
