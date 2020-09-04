package uk.gov.justice.digital.hmpps.pecs.jpc.location

import org.springframework.data.repository.CrudRepository
import java.util.*

interface LocationRepository : CrudRepository<Location, UUID> {

    fun findBySiteName(siteName: String) : Location?
}