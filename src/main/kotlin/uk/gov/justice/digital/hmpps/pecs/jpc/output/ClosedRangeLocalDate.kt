package uk.gov.justice.digital.hmpps.pecs.jpc.output

import java.lang.IllegalArgumentException
import java.time.LocalDate

/**
 * throws [IllegalArgumentException] if [start] is after [endInclusive].
 */
class ClosedRangeLocalDate(override val start: LocalDate, override val endInclusive: LocalDate) : ClosedRange<LocalDate> {
    init {
        if (start.isAfter(endInclusive)) throw IllegalArgumentException("start cannot be after end date.")
    }
}