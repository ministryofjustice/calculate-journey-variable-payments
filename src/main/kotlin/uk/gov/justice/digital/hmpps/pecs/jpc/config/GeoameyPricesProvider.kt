package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

/**
 * Responsible for providing the Geoamey prices Excel spreadsheet via an [InputStream].
 */
fun interface GeoameyPricesProvider {
    /**
     * The caller is responsible for closing the [InputStream].
     */
    fun get(): InputStream
}
