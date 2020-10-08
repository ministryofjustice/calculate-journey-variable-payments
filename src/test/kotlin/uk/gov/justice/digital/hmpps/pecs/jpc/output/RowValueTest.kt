package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

internal class RowValueTest {
    val from = LocalDate.of(2020, 9, 10)
    val to = LocalDate.of(2020, 9, 11)

    val cancelledJourney = JourneyWithEvents(journeyFactory(journeyId = "J1M1", state = JourneyState.CANCELLED.value, billable = false, vehicleRegistration = "V1"),
            listOf(
                    journeyEventFactory(type = EventType.JOURNEY_START.value),
                    journeyEventFactory(type = EventType.JOURNEY_CANCEL.value, notes = "cancelled due to fog"),
                    journeyEventFactory(type = EventType.JOURNEY_CANCEL.value, notes = "off because of sun")
            )

    )
    val cancelledJourneyPrice = JourneyPrice(cancelledJourney, null)


    val completedJourney = JourneyWithEvents(journeyFactory(journeyId = "J2M1", billable = true, vehicleRegistration = null),
            listOf(journeyEventFactory(type = EventType.JOURNEY_COMPLETE.value)))
    val completedJourneyPrice = JourneyPrice(completedJourney, 50)


    val moveWith2Journeys = MoveReport(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(eventId = "1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                    moveEventFactory(eventId = "2", type = EventType.MOVE_COMPLETE.value, notes = "finished", occurredAt = from.atStartOfDay().plusHours(10))
            ),
            journeysWithEvents = listOf(cancelledJourney, completedJourney)
    )



    @Test
    fun `Render MovePrice with sitenames and 2 journeys, one with no vehicle id`() {

        val priceForMoveWith2Journeys = MovePrice(moveWith2Journeys, listOf(cancelledJourneyPrice, completedJourneyPrice))
        val moveRow = RowValue.forMovePrice(priceForMoveWith2Journeys)

        assertThat(moveRow).isEqualTo(RowValue(
                "UKW4591N",
                "from",
                "PR",
                "to",
                "CO",
                "10/09/2020",
                "05:00",
                "10/09/2020",
                "10:00",
                "V1, NOT GIVEN",
                "PRISON1",
                null,
                "",
                "* finished"
        ))
    }

    @Test
    fun `Render JourneyPrice for cancelled journey`() {

        val cancelledJourneyRow = RowValue.forJourneyPrice(1, cancelledJourneyPrice)

        assertThat(cancelledJourneyRow).isEqualTo(RowValue(
                "Journey 1",
                "from",
                "PR",
                "to",
                "CO",
                "16/06/2020",
                "10:20",
                "CANCELLED",
                "CANCELLED",
                "V1",
                null,
                null,
                "NO",
                "* cancelled due to fog\n* off because of sun"
        ))
    }


    @Test
    fun `Render JourneyPrice for completed journey`() {

        val completedJourneyRow = RowValue.forJourneyPrice(2, completedJourneyPrice)

        assertThat(completedJourneyRow).isEqualTo(RowValue(
                "Journey 2",
                "from",
                "PR",
                "to",
                "CO",
                null,
                null,
                "16/06/2020",
                "10:20",
                null,
                null,
                0.5,
                "YES",
                ""
        ))
    }


    @Test
    fun `Render MovePrice without sitenames`() {
        val noMappedFrom = noLocationFactory()
        val noMappedTo = noLocationFactory()
        val journey = JourneyWithEvents(journeyFactory(journeyId = "J1M1", fromLocation = noMappedFrom, toLocation = noMappedTo, billable = true, vehicleRegistration = "V1"))

        val moveWithMappedLocations = MoveReport(
                move = moveFactory(fromLocation = noMappedFrom, toLocation = noMappedTo),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(journey)
        )
        val price = MovePrice(moveWithMappedLocations, listOf(JourneyPrice(journey, 80)))

        val rowValue = RowValue.forMovePrice(price)

        assertThat(rowValue).isEqualTo(RowValue(
                "UKW4591N",
                "NOT_MAPPED_AGENCY_ID",
                "UNKNOWN",
                "NOT_MAPPED_AGENCY_ID",
                "UNKNOWN",
                "10/09/2020",
                "05:00",
                "10/09/2020",
                "10:00",
                "V1",
                "PRISON1",
                0.8,
                "",
                ""
        ))
    }
}