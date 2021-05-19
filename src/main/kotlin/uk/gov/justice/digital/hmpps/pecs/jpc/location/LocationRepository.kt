package uk.gov.justice.digital.hmpps.pecs.jpc.location

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LocationRepository : JpaRepository<Location, UUID> {
  fun findFirstByOrderByUpdatedAtDesc(): Location?

  fun findByNomisAgencyId(id: String): Location?

  fun findBySiteName(name: String): Location?

  fun findByNomisAgencyIdOrSiteName(id: String, name: String): List<Location>
}
