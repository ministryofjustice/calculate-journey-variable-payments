package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

data class PricesSpreadsheetError(val supplier: Supplier, val row: Int, val error: Throwable)