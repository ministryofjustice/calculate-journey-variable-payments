package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

interface SpreadsheetProvider {
  /**
   * The consumer of the supplied [InputStream] is responsible for closing it. Failure to close it can lead to resource issues.
   */
  fun get(resourceName: String) : InputStream
}