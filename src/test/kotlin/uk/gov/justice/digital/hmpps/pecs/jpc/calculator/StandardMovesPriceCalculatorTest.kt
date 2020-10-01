package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate


@ActiveProfiles("test")
internal class StandardMovesPriceCalculatorTest{

    private val locationRepository: LocationRepository = mock { on {findAll()} doReturn listOf(
            locationFactory(nomisAgencyId = "WYI", siteName = "from"),
            locationFactory(nomisAgencyId = "GNI", siteName = "to")
            )
        }

    private val standardMovePrice = priceFactory(fromSiteName = "from", toSiteName = "to", priceInPence = 101)
    private val priceRepository: PriceRepository = mock { on {findAll()} doReturn listOf(standardMovePrice)}

    private val calculatorFactory = PriceCalculatorFactory(locationRepository, priceRepository)

    @Test
    fun `price key for Price should be $fromSiteName-$SiteName`(){
        assertThat(priceKey(standardMovePrice)).isEqualTo("from-to")
    }

    @Test
    fun `price key for Journey should be $fromSiteName-$toSiteName`(){
        assertThat(calculatorFactory.calculator(listOf()).priceKey(journeyFactory(fromLocation = "WYI", toLocation = "GNI"))).isEqualTo("from-to")
    }

    @Test
    fun `Standard moves priced correctly`() {
        val movesFrom = LocalDate.of(2020, 9, 10)
        val movesTo = LocalDate.of(2020, 9, 11)

        val completedMoveWithPricedJourney = MovePersonJourneysEvents(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(moveEventFactory(
                        type = EventType.MOVE_COMPLETE.value, occurredAt = movesFrom.atStartOfDay())
                ),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true), listOf()))
        )

        val completedMoveWithUnpricedJourney = MovePersonJourneysEvents(
                move = moveFactory(moveId = "M2", fromLocation = "NOTPRICED"),
                person = personFactory(),
                events =  listOf(moveEventFactory(
                        type = EventType.MOVE_COMPLETE.value, moveId = "M2", occurredAt = movesTo.atStartOfDay())
                ),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true, fromLocation = "NOTPRICED"), listOf()))
        )


        val calculator = calculatorFactory.calculator(listOf(completedMoveWithPricedJourney, completedMoveWithUnpricedJourney))

        val prices = calculator.standardPrices(MoveFiltererParams(Supplier.SERCO, movesFrom, movesTo)).toList()

        // Both moves should be standard moves
        assertThat(prices.map { it.movePersonJourneysEvents.move.id }).isEqualTo(listOf("M1", "M2"))

        // M1 price should be 101p
        assertThat(prices[0].totalInPence()).isEqualTo(101)

        // M2 price should not be set
        assertThat(prices[1].totalInPence()).isNull()
    }
}