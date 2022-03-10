package uk.gov.justice.digital.hmpps.pecs.jpc.util

import java.time.LocalDate

/**
 * throws [IllegalArgumentException] if [start] is after [endInclusive].
 */
data class DateRange(override val start: LocalDate, override val endInclusive: LocalDate) : ClosedRange<LocalDate> {
  init {
    if (start.isAfter(endInclusive)) throw IllegalArgumentException("start cannot be after end date.")
  }

  fun listOf() = recurse(listOf(DateRange(start, start.endOfMonth())))

  private fun recurse(ranges: List<DateRange>): List<DateRange> {
    val last = ranges.last()

    if (last.endInclusive == endInclusive || last.endInclusive.isAfter(endInclusive)) {
      return ranges.subList(0, ranges.size - 1).toMutableList() + DateRange(endInclusive.startOfMonth(), endInclusive)
    }

    return recurse(
      ranges.toMutableList() + DateRange(
        last.start.plusMonths(1).startOfMonth(),
        last.start.plusMonths(1).endOfMonth()
      )
    )
  }

  private fun LocalDate.startOfMonth(): LocalDate = this.withDayOfMonth(1)

  private fun LocalDate.endOfMonth(): LocalDate = this.startOfMonth().plusMonths(1).minusDays(1)
}
