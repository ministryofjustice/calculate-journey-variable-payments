package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

data class LocationsSpreadsheetError(val locationTab: LocationsSpreadsheet.Tab, val row: Int, val error: Throwable)