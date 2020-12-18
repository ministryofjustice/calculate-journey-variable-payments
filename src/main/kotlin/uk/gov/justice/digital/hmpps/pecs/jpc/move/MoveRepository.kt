package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier


interface MoveRepository : JpaRepository<Move, String>{

    fun findAllByMoveIdIn(ids: List<String>): List<Move>

    fun findByReferenceAndSupplier(ref: String, supplier: Supplier) : Optional<Move>
}