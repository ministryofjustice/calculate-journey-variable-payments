package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JourneyTest {

  @Test
  fun `given a completed journey the registration should come from the journey when missing from events`() {
    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(
          journeyEventJE1(eventType = EventType.JOURNEY_START),
          journeyEventJE1(eventId = "JE2", eventType = EventType.JOURNEY_COMPLETE)
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_REG")
    }
  }

  @Test
  fun `given a completed journey the registration should come from the journey start event when present`() {
    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(
          journeyEventJE1(eventType = EventType.JOURNEY_START, details = mapOf("vehicle_reg" to "JOURNEY_START_REG")),
          journeyEventJE1(eventId = "JE2", eventType = EventType.JOURNEY_COMPLETE)
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_START_REG")
    }
  }

  @Test
  fun `given a completed journey the registration should come from the journey complete event when present`() {
    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(
          journeyEventJE1(eventType = EventType.JOURNEY_START),
          journeyEventJE1(eventId = "JE2", eventType = EventType.JOURNEY_COMPLETE, details = mapOf("vehicle_reg" to "JOURNEY_COMPLETE_REG"))
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_COMPLETE_REG")
    }
  }

  @Test
  fun `given a completed journey the registrations should come from the journey start and complete events when present`() {
    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(
          journeyEventJE1(eventType = EventType.JOURNEY_START, details = mapOf("vehicle_reg" to "JOURNEY_START_REG")),
          journeyEventJE1(
            eventId = "JE2",
            eventType = EventType.JOURNEY_COMPLETE,
            details = mapOf("vehicle_reg" to "JOURNEY_COMPLETE_REG")
          )
        )
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_START_REG, JOURNEY_COMPLETE_REG")
    }
  }

  @Test
  fun `given a completed journey the same registrations on the events should only return one registration`() {
    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(
          journeyEventJE1(eventType = EventType.JOURNEY_START, details = mapOf("vehicle_reg" to "SAME_JOURNEY_EVENT_REG")),
          journeyEventJE1(
            eventId = "JE2",
            eventType = EventType.JOURNEY_COMPLETE,
            details = mapOf("vehicle_reg" to "SAME_JOURNEY_EVENT_REG")
          )
        )
      )
    ) {
      assertThat(registration()).isEqualTo("SAME_JOURNEY_EVENT_REG")
    }
  }

  @Test
  fun `given a completed journey the registrations should come ordered from journey start and complete events when present`() {
    val journeyStartEvent =
      journeyEventJE1(eventType = EventType.JOURNEY_START, details = mapOf("vehicle_reg" to "JOURNEY_START_REG"))

    val journeyCompleteEvent = journeyEventJE1(
      eventId = "JE2",
      eventType = EventType.JOURNEY_COMPLETE,
      details = mapOf("vehicle_reg" to "JOURNEY_COMPLETE_REG",),
      occurredAt = journeyStartEvent.occurredAt.plusMinutes(1)
    )

    with(
      journeyJ1(
        vehicleRegistration = "JOURNEY_REG",
        events = listOf(journeyCompleteEvent, journeyStartEvent)
      )
    ) {
      assertThat(registration()).isEqualTo("JOURNEY_START_REG, JOURNEY_COMPLETE_REG")
    }
  }
}
