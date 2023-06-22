package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

interface MoveRepository : JpaRepository<Move, String> {

  fun findAllByMoveIdIn(ids: List<String>): List<Move>

  fun findByReferenceAndSupplier(ref: String, supplier: Supplier): Move?

  @Query(
    """
    select m
      from Move m
     where m.supplier = :supplier
       and m.moveYear = :year
       and m.moveMonth = :month
       and upper(m.status) = 'COMPLETED'
       and (m.moveType is null or m.dropOffOrCancelledDateTime is null)
  order by m.moveDate, m.pickUpDateTime
  """,
  )
  fun findCompletedCandidateReconcilableMoves(
    @Param("supplier") supplier: Supplier,
    @Param("year") year: Int,
    @Param("month") month: Int,
  ): List<Move>
}
