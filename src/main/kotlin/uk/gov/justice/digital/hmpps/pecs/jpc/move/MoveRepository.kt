package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MoveRepository : JpaRepository<Move, String>{

    fun findByProfileId(profileId : String): Optional<Move>
}