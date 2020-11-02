package uk.gov.justice.digital.hmpps.pecs.jpc.import.location

data class LocationsSpreadsheetError(val locationTab: LocationsSpreadsheet.Tab, val row: Int, val error: Throwable)