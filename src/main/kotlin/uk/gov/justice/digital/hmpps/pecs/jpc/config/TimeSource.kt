package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Simple SAM interface to enable easier control of time with the code (and unit tests).
 */
fun interface TimeSource {
  fun dateTime(): LocalDateTime

  fun date(): LocalDate = dateTime().toLocalDate()
}
