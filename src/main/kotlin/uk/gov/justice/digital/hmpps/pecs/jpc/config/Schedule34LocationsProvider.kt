package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

/**
 * Responsible for providing the Schedule 34 locations Excel spreadsheet via an [InputStream].
 */
fun interface Schedule34LocationsProvider {
    fun get(): InputStream
}