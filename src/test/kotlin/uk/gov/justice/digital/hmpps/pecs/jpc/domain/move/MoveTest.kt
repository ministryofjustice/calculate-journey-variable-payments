package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType

class MoveTest {

  @Test
  fun `given a move with a completed journey the registration should come from the journey`() {
    with(
      moveM1(
        journeys = listOf(
          journeyJ1(
            vehicleRegistration = "JOURNEY_REG",
            events = listOf(
              journeyEventJE1(eventType = EventType.JOURNEY_START),
              journeyEventJE1(eventId = "JE2", eventType = EventType.JOURNEY_COMPLETE)
            )
          )
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_REG")
    }
  }

  @Test
  fun `given a move with a completed journey the registration should come from the journey start event`() {
    with(
      moveM1(
        journeys = listOf(
          journeyJ1(
            vehicleRegistration = "JOURNEY_REG",
            events = listOf(
              journeyEventJE1(eventType = EventType.JOURNEY_START, details = mapOf("vehicle_reg" to "JOURNEY_START_REG")),
              journeyEventJE1(eventId = "JE2", eventType = EventType.JOURNEY_COMPLETE)
            )
          )
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_START_REG")
    }
  }

  @Test
  fun `given a move with two journeys and different vehicles there should be two vehicle registrations`() {
    with(
      moveM1(
        journeys = listOf(
          journeyJ1(vehicleRegistration = "JOURNEY_ONE_REG"),
          journeyJ1(journeyId = "J2", vehicleRegistration = "JOURNEY_TWO_REG")
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_ONE_REG, JOURNEY_TWO_REG")
    }
  }

  @Test
  fun `given a move with two journeys and the same vehicle there should be one vehicle registration`() {
    with(
      moveM1(
        journeys = listOf(
          journeyJ1(vehicleRegistration = "SAME_REG"),
          journeyJ1(journeyId = "J2", vehicleRegistration = "SAME_REG")
        )
      )
    ) {
      assertThat(registration()).isEqualTo("SAME_REG")
    }
  }
}
