package uk.gov.justice.digital.hmpps.pecs.jpc.config

import java.io.InputStream

/**
 * Responsible for providing the Serco prices Excel spreadsheet via an [InputStream].
 */
fun interface SercoPricesProvider{
    fun get(): InputStream
}