package uk.gov.justice.digital.hmpps.pecs.jpc.location

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LocationRepository : JpaRepository<Location, UUID> {

  fun findByNomisAgencyId(id: String): Location?

  fun findBySiteName(name: String): Location?
}
