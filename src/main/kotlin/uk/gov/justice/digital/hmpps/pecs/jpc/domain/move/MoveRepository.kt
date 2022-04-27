package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

interface MoveRepository : JpaRepository<Move, String> {

  fun findAllByMoveIdIn(ids: List<String>): List<Move>

  fun findByReferenceAndSupplier(ref: String, supplier: Supplier): Move?
}
