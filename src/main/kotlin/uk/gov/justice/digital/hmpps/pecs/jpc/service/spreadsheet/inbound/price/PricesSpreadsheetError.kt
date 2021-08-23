package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

data class PricesSpreadsheetError(val supplier: Supplier, val row: Int, val error: Throwable)
