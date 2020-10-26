package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface MoveModelRepository : CrudRepository<MoveModel, String> {

    @Query("select m from MoveModel m left join m.journeys j")
    override fun findAll(): MutableIterable<MoveModel>

    @Query("select m from MoveModel m " +
            " left join m.fromLocation mfl " +
            " left join m.toLocation mtl " +
            " left join fetch m.journeys j" +
            " where m.moveId = ?1")
    override fun findById(id: String): Optional<MoveModel>
}