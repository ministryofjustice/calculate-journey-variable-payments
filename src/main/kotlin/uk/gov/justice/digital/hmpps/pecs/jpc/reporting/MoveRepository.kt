package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.data.repository.CrudRepository
import java.util.*

interface MoveRepository : CrudRepository<Move, UUID> {

}