package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.springframework.data.repository.CrudRepository
import java.util.*

interface PriceRepository : CrudRepository<Price, UUID> {
}