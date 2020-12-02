package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository

interface MoveRepository : JpaRepository<Move, String>