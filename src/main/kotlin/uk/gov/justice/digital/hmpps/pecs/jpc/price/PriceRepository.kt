package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.util.UUID
import java.util.stream.Stream

interface PriceRepository : JpaRepository<Price, UUID> {
  fun deleteBySupplier(supplier: Supplier): Long

  fun findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
    supplier: Supplier,
    from: Location,
    to: Location,
    effectiveYear: Int
  ): Price?

  fun findBySupplierAndFromLocationAndToLocation(supplier: Supplier, from: Location, to: Location): Price?

  fun findBySupplierAndEffectiveYear(supplier: Supplier, effectiveYear: Int): Stream<Price>

  fun deleteBySupplierAndEffectiveYear(supplier: Supplier, effectiveYear: Int)
}
