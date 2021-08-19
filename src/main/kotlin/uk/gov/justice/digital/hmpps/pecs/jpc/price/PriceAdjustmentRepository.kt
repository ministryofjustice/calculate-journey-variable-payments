package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PriceAdjustmentRepository : JpaRepository<PriceAdjustment, UUID> {
  fun findBySupplier(supplier: Supplier): PriceAdjustment?

  fun existsPriceAdjustmentBySupplier(supplier: Supplier): Boolean
}
