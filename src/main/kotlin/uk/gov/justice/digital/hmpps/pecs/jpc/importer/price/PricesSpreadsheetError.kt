package uk.gov.justice.digital.hmpps.pecs.jpc.importer.price

import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

data class PricesSpreadsheetError(val supplier: Supplier, val row: Int, val error: Throwable)
