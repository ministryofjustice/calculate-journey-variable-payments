package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

data class LocationImportError(val locationType: LocationType, val row: Int, val error: Throwable)