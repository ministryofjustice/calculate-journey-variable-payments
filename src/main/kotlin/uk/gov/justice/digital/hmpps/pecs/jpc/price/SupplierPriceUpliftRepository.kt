package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SupplierPriceUpliftRepository : JpaRepository<SupplierPriceUplift, UUID> {
  fun findBySupplier(supplier: Supplier): SupplierPriceUplift?

  fun deleteBySupplier(supplier: Supplier)
}
