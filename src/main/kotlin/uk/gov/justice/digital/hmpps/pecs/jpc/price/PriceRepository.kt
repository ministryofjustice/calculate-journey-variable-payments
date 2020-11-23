package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PriceRepository : JpaRepository<Price, UUID> {
    fun deleteBySupplier(supplier: Supplier): Long
}
