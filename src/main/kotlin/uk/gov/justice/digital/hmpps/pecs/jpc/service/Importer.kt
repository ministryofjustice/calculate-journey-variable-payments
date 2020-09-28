package uk.gov.justice.digital.hmpps.pecs.jpc.service

interface Importer<T> {
    fun import(): T?
}