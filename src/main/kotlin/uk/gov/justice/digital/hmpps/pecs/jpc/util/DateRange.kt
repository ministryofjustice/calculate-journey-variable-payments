package uk.gov.justice.digital.hmpps.pecs.jpc.util

import java.time.LocalDate

/**
 * throws [IllegalArgumentException] if [start] is after [endInclusive].
 */
data class DateRange(override val start: LocalDate, override val endInclusive: LocalDate) : ClosedRange<LocalDate> {
  init {
    if (start.isAfter(endInclusive)) throw IllegalArgumentException("start cannot be after end date.")
  }

  /**
   * Will return a list of one or more ranges depending on the duration of the range. For example 1st Jan 2020 to 25th
   * Feb 2020 will return a list of two ranges, one from the 1st Jan to 31t Jan 2020 and the other from the 1st  Feb to
   * the 25th Feb 2020.
   */
  fun listOf() = recurse(listOf(DateRange(start, start.endOfMonth())))

  private tailrec fun recurse(ranges: List<DateRange>): List<DateRange> {
    val last = ranges.last()

    return if (last.endInclusive == endInclusive || last.endInclusive.isAfter(endInclusive))
      return ranges.subList(0, ranges.size - 1).toMutableList() + DateRange(endInclusive.startOfMonth(), endInclusive)
    else recurse(
      ranges.toMutableList() + DateRange(
        last.start.plusMonths(1).startOfMonth(),
        last.start.plusMonths(1).endOfMonth()
      )
    )
  }

  private fun LocalDate.startOfMonth(): LocalDate = this.withDayOfMonth(1)

  private fun LocalDate.endOfMonth(): LocalDate = this.startOfMonth().plusMonths(1).minusDays(1)
}
