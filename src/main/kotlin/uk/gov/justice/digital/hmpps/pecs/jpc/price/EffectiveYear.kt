package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.time.LocalDate

/**
 * The effective year represents the start year’s pricing that should be applied. An effective year starts at the
 * beginning of September and runs to the end of August the following year. For example, the 1st Sept 2020 to 31st Aug
 * 2021 would be be 2020.
 */
@Component
class EffectiveYear(private val timeSource: TimeSource) {
  fun current(): Int = effectiveYearForDate(timeSource.date())
}

fun effectiveYearForDate(date: LocalDate) = if (date.monthValue >= 9) date.year else date.year - 1

fun nextEffectiveYearForDate(date: LocalDate) = effectiveYearForDate(date) + 1
