package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

data class LocationImportError(val locationTab: LocationTab, val row: Int, val error: Throwable)