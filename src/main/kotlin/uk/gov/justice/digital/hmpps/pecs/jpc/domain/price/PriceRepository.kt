package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import java.util.UUID
import java.util.stream.Stream

interface PriceRepository : JpaRepository<Price, UUID> {
  fun findBySupplierAndFromLocationAndToLocationAndEffectiveYear(
    supplier: Supplier,
    from: Location,
    to: Location,
    effectiveYear: Int
  ): Price?

  fun findBySupplierAndFromLocationAndToLocation(supplier: Supplier, from: Location, to: Location): Price?

  fun findBySupplierAndEffectiveYear(supplier: Supplier, effectiveYear: Int): Stream<Price>
}
