package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDateTime

class MoveTest {

  private val journeyOne: Journey = mock { on { vehicleRegistration } doReturn "ABC" }

  private val journeyTwo: Journey = mock { on { vehicleRegistration } doReturn "DEF" }

  @Test
  fun `given a move with one journey there should be one vehicle registration`() {
    val moveWithSingleJourney = move().copy(journeys = listOf(journeyOne))

    assertThat(moveWithSingleJourney.registration()).isEqualTo("ABC")
  }

  @Test
  fun `given a move with two journeys and different vehicles there should be two vehicle registrations`() {
    val moveWithTwoJourneys = move().copy(journeys = listOf(journeyOne, journeyTwo))

    assertThat(moveWithTwoJourneys.registration()).isEqualTo("ABC, DEF")
  }

  @Test
  fun `given a move with two journeys and the same vehicle there should be one vehicle registration`() {
    val moveWithTwoJourneys = move().copy(journeys = listOf(journeyOne, journeyOne))

    assertThat(moveWithTwoJourneys.registration()).isEqualTo("ABC")
  }

  private fun move() = Move(
    moveId = "MOVE_ID",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.SERCO,
    status = MoveStatus.booked,
    reference = "MOVE_REF",
    fromNomisAgencyId = "FROM_AGENCY",
    reportFromLocationType = "prison"
  )
}
