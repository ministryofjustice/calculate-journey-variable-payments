package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

/**
 * Responsible for providing the Serco prices Excel spreadsheet via an [InputStream].
 */
fun interface SercoPricesProvider {
  /**
   * The caller is responsible for closing the [InputStream].
   */
  fun get(): InputStream
}
