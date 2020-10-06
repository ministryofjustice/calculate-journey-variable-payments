package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

internal class StandardValuesTest {
    val from = LocalDate.of(2020, 9, 10)
    val to = LocalDate.of(2020, 9, 11)

    val journey1 = JourneyWithEvents(journeyFactory(journeyId = "J1M1", billable = true, vehicleRegistration = "V1"))
    val journey2 = JourneyWithEvents(journeyFactory(journeyId = "J2M1", billable = true, vehicleRegistration = null))

    private val standardInDateRange = MoveReport(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                    moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
            ),
            journeysWithEvents = listOf(journey1, journey2)
    )

    @Test
    fun `MovePrice with sitenames and 2 journeys, one with no vehicle id, rendered correctly`() {
        val from = fromLocationFactory()
        val to = toLocationFactory()
        val price = MovePrice(from, to, standardInDateRange, listOf(
                JourneyPrice(journey1, 100),
                JourneyPrice(journey2, 50)

        ))
        val vals = StandardValues.fromMovePrice(price)

        assertThat(vals).isEqualTo(StandardValues(
                "UKW4591N",
                from.siteName,
                from.locationType.name,
                to.siteName,
                to.locationType.name,
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
        val price = MovePrice(null, null, standardInDateRange, listOf(JourneyPrice(journey1, 80)))

        val vals = StandardValues.fromMovePrice(price)

        assertThat(vals).isEqualTo(StandardValues(
                "UKW4591N",
                "WYI",
                "NOT MAPPED",
                "GNI",
                "NOT MAPPED",
                "10/09/2020",
                "05:00",
                "10/09/2020",
                "10:00",
                "V1, NOT GIVEN",
                "PRISON1",
                80
        ))
    }
}