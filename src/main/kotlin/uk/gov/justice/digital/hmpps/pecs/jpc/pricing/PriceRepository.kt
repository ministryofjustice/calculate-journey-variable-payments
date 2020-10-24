package uk.gov.justice.digital.hmpps.pecs.jpc.pricing

import org.springframework.data.repository.CrudRepository
import java.util.*

interface PriceRepository : CrudRepository<Price, UUID> {
    fun findAllBySupplier(supplier: Supplier): List<Price>
}