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


    @Test
    fun `MovePrice with sitenames and 2 journeys, one with no vehicle id, rendered correctly`() {
        val journey1 = JourneyWithEvents(journeyFactory(journeyId = "J1M1", billable = true, vehicleRegistration = "V1"))
        val journey2 = JourneyWithEvents(journeyFactory(journeyId = "J2M1", billable = true, vehicleRegistration = null))

        val moveWithMappedLocations = MoveReport(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(journey1, journey2)
        )

        val price = MovePrice(moveWithMappedLocations, listOf(
                JourneyPrice(journey1, 100),
                JourneyPrice(journey2, 50)

        ))
        val moveRow = RowValue.forMovePrice(price)

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
                150
        ))
    }


    @Test
    fun `MovePrice without sitenames rendered correctly`() {
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

        val vals = RowValue.forMovePrice(price)

        assertThat(vals).isEqualTo(RowValue(
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
                80
        ))
    }
}