package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

/**
 * Simple DTO to capture the numbers persisted (if any) and the number of errors (if any).
 */
data class PersistenceResult(val persisted: Int = 0, val errors: Int = 0) {
  /**
   * Will be zero if nothing processed.
   */
  fun processed() = persisted + errors
}
