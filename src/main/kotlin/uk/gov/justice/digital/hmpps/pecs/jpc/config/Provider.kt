package uk.gov.justice.digital.hmpps.pecs.jpc.config

fun interface Provider<T> {
    fun get(resourceName: String) : T
}