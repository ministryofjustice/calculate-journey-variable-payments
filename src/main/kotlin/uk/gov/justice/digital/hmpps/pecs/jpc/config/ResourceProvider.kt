package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

interface ResourceProvider {
  fun get(resourceName: String) : InputStream
}