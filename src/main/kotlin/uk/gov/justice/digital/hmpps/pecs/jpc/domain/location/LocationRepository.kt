package uk.gov.justice.digital.hmpps.pecs.jpc.domain.location

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.stream.Stream

interface LocationRepository : JpaRepository<Location, UUID> {
  fun findFirstByOrderByUpdatedAtDesc(): Location?

  fun findByNomisAgencyId(id: String): Location?

  fun findBySiteName(name: String): Location?

  fun findByNomisAgencyIdOrSiteName(id: String, name: String): List<Location>

  /**
   * Callers of this must remember to close the [Stream].
   */
  fun findAllByOrderBySiteName(): Stream<Location>
}
