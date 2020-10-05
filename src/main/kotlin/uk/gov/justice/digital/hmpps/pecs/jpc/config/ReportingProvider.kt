package uk.gov.justice.digital.hmpps.pecs.jpc.config

fun interface ReportingProvider {
    fun get(resourceName: String) : String
}