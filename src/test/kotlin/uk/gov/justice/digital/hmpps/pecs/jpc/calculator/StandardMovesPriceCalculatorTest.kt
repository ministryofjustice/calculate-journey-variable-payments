package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate


@ActiveProfiles("test")
internal class StandardMovesPriceCalculatorTest{

    private val from = fromLocationFactory()
    private val to = toLocationFactory()

    private val standardMovePrice = priceFactory(fromSiteName = "from", toSiteName = "to", priceInPence = 101)
    private val priceRepository: PriceRepository = mock { on {findAll()} doReturn listOf(standardMovePrice)}

    private val calculatorFactory = PriceCalculatorFactory(priceRepository)

    @Test
    fun `price key for Price should be $fromSiteName-$SiteName`(){
        assertThat(priceKey(standardMovePrice)).isEqualTo("from-to")
    }

    @Test
    fun `price key for Journey should be $fromSiteName-$toSiteName`(){
        assertThat(calculatorFactory.calculator(listOf()).priceKey(journeyFactory())).isEqualTo("from-to")
    }

    @Test
    fun `Standard moves priced correctly`() {
        val movesFrom = LocalDate.of(2020, 9, 10)
        val movesTo = LocalDate.of(2020, 9, 11)

        val completedMoveWithPricedJourney = MoveReport(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(moveEventFactory(
                        type = EventType.MOVE_COMPLETE.value, occurredAt = movesFrom.atStartOfDay())
                ),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true), listOf()))
        )

        val completedMoveWithUnpricedJourney = MoveReport(
                move = moveFactory(moveId = "M2", fromLocation = fromLocationFactory(nomisAgencyId = "NOTPRICED")),
                person = personFactory(),
                events =  listOf(moveEventFactory(
                        type = EventType.MOVE_COMPLETE.value, moveId = "M2", occurredAt = movesTo.atStartOfDay())
                ),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true,
                        fromLocation = fromLocationFactory(nomisAgencyId = "NOTPRICED", siteName = "Not priced site")), listOf()))
        )


        val calculator = calculatorFactory.calculator(listOf(completedMoveWithPricedJourney, completedMoveWithUnpricedJourney))

        val prices = calculator.standardPrices(FilterParams(Supplier.SERCO, movesFrom, movesTo)).toList()

        // Both moves should be standard moves
        assertThat(prices.map { it.moveReport.move.id }).isEqualTo(listOf("M1", "M2"))

        with(prices[0]){ // M1
            assertThat(totalInPence()).isEqualTo(101)
            assertThat(moveReport.move.fromLocation.siteName).isEqualTo(from.siteName)
            assertThat(moveReport.move.toLocation?.siteName).isEqualTo(to.siteName)
        }

        // M2 price should not be set
        assertThat(prices[1].totalInPence()).isNull()
    }
}