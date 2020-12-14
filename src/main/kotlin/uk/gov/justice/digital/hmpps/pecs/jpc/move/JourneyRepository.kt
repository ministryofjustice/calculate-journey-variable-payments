package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.repository.CrudRepository
import java.util.*

interface JourneyRepository : CrudRepository<Journey, String> {

    fun findAllByMoveId(moveId: String) : List<Journey>
}