package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.location

data class LocationsSpreadsheetError(val locationTab: LocationsSpreadsheet.Tab, val row: Int, val error: Throwable)
