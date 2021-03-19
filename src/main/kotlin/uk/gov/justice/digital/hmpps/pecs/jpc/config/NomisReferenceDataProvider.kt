package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

/**
 * Responsible for providing the NOMIS locations reference data via an [InputStream].
 */
fun interface NomisReferenceDataProvider {
  /**
   * The caller is responsible for closing the [InputStream].
   */
  fun get(): InputStream
}
