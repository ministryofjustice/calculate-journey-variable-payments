package uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey

import org.springframework.data.jpa.repository.JpaRepository

interface JourneyRepository : JpaRepository<Journey, String> {

  fun findAllByMoveId(moveId: String): List<Journey>

  fun findAllByMoveIdIn(moveIds: List<String>): List<Journey>
}
