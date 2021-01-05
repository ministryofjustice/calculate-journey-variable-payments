package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.util.UUID

interface PriceRepository : JpaRepository<Price, UUID> {
  fun deleteBySupplier(supplier: Supplier): Long

  fun findBySupplierAndFromLocationAndToLocation(supplier: Supplier, from: Location, to: Location): Price?

  fun findBySupplier(supplier: Supplier): List<Price>
}
