package uk.gov.justice.digital.hmpps.pecs.jpc.config

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.util.stream.Stream

fun interface SupplierPrices {
  fun get(supplier: Supplier, effectiveYear: Int): Stream<Price>
}
