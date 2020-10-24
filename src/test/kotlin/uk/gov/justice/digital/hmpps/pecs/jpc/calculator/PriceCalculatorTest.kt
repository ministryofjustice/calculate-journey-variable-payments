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
internal class PriceCalculatorTest{

    private val from = fromPrisonNomisAgencyId()
    private val to = toCourtNomisAgencyId()

    private val standardMovePrice = priceFactory(priceInPence = 101)
    private val priceRepository: PriceRepository = mock { on {findAllBySupplier(Supplier.SERCO)} doReturn listOf(standardMovePrice)}

    private val calculator = PriceCalculator(priceRepository)

    val movesFrom = LocalDate.of(2020, 9, 10)
    val movesTo = LocalDate.of(2020, 9, 11)

    private val completedMoveWithPricedBillableJourney = Report(
            move = moveFactory(),
            person = personFactory(),
            events = listOf(moveEventFactory(
                    type = EventType.MOVE_COMPLETE.value, occurredAt = movesFrom.atStartOfDay())
            ),
            journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true), listOf()))
    )


    private val completedMoveWithUnpricedJourney = Report(
            move = moveFactory(moveId = "M2", fromLocation = "NOTPRICED"),
            person = personFactory(),
            events =  listOf(moveEventFactory(
                    type = EventType.MOVE_COMPLETE.value, moveId = "M2", occurredAt = movesTo.atStartOfDay())
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(billable = true, fromLocation = "NOTPRICED"), listOf())
            )
    )

    private val redirectMoveWithUnbillableJourney = Report(
            move = moveFactory(moveId = "M3"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(moveId = "M3", type = EventType.MOVE_START.value, occurredAt = movesFrom.atStartOfDay()),
                    moveEventFactory(moveId = "M3", type = EventType.MOVE_REDIRECT.value, occurredAt = movesFrom.atStartOfDay().plusHours(5)),
                    moveEventFactory(moveId = "M3", type = EventType.MOVE_COMPLETE.value, occurredAt = movesFrom.atStartOfDay().plusHours(10))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(moveId = "M3", billable = true), listOf()),
                    JourneyWithEvents(journeyFactory(moveId = "M3", billable = false), listOf()))
    )




    private val mutliTypeMove = Report(
            move = moveFactory(moveId = "M4"),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(moveId = "M4", type = EventType.MOVE_START.value, occurredAt = movesFrom.atStartOfDay()),
                    moveEventFactory(moveId = "M4", type = EventType.MOVE_REDIRECT.value, occurredAt = movesFrom.atStartOfDay().plusHours(5)),
                    moveEventFactory(moveId = "M4", type = EventType.MOVE_LODGING_START.value, occurredAt = movesFrom.atStartOfDay().plusHours(5)),
                    moveEventFactory(moveId = "M4", type = EventType.MOVE_COMPLETE.value, occurredAt = movesFrom.atStartOfDay().plusHours(10))
            ),
            journeysWithEvents = listOf(
                    JourneyWithEvents(journeyFactory(moveId = "M4", billable = true), listOf()),
                    JourneyWithEvents(journeyFactory(moveId = "M4", billable = false), listOf()))
    )

    private val cancelledBillable = Report(
            move = moveFactory(
                    moveId = "M9",
                    status = MoveStatus.CANCELLED.value,
                    fromLocation = fromPrisonNomisAgencyId(),
                    fromLocationType = "prison",
                    toLocation = toCourtNomisAgencyId(),
                    toLocationType = "prison",
                    cancellationReason = "cancelled_by_pmu",
                    date = movesTo
            ),
            person = personFactory(),
            events = listOf(
                    moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = movesTo.atStartOfDay().minusHours(24)),
                    moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = movesTo.atStartOfDay().minusHours(2))
            ),
            journeysWithEvents = listOf()
    )

    private val moves = listOf(completedMoveWithPricedBillableJourney, redirectMoveWithUnbillableJourney, completedMoveWithUnpricedJourney, mutliTypeMove, cancelledBillable)
    private val params = FilterParams(Supplier.SERCO, movesFrom, movesTo)
    private val movePrices = calculator.allPrices(params, moves)

    @Test
    fun `price key for Price should be $fromSiteName-$SiteName`(){
        assertThat(standardMovePrice.journey()).isEqualTo("WYI-GNI")
    }

    @Test
    fun `price key for Journey should be $fromSiteName-$toSiteName`(){
        assertThat(calculator.priceKey(journeyFactory())).isEqualTo("WYI-GNI")
    }

    @Test
    fun `Standard moves priced correctly`() {
        val standardPricesWithSummary = movePrices.withType(MovePriceType.STANDARD)
        val standardPrices = standardPricesWithSummary.prices

        // M1 and M2 should be standard moves
        assertThat(standardPrices.map { it.report.move.id }).containsExactly("M1", "M2")

        with(standardPrices[0]) { // M1
            assertThat(totalInPence()).isEqualTo(101)
            assertThat(report.move.fromLocation).isEqualTo(from)
            assertThat(report.move.toLocation).isEqualTo(to)
        }

        // M2 price should not be set
        assertThat(standardPrices[1].totalInPence()).isNull()

        // Summary values
        assertThat(standardPricesWithSummary.summary.percentage).isEqualTo(0.4)
        assertThat(standardPricesWithSummary.summary.volume).isEqualTo(2)
    }

    @Test
    fun `Multi-type moves priced correctly`(){

        val multi = movePrices.withType(MovePriceType.MULTI)
        assertThat(multi.prices.map{it.report.move.id}).containsExactly("M3", "M4")
        assertThat(multi.summary.percentage).isEqualTo(0.4)
        assertThat(multi.summary.volume).isEqualTo(2)
        assertThat(multi.summary.volumeUnpriced).isEqualTo(2)
        assertThat(multi.summary.totalPriceInPence).isEqualTo(0)
    }

    @Test
    fun `Cancelled moves priced correctly`() {
        val cancelled = movePrices.withType(MovePriceType.CANCELLED)

        assertThat(cancelled.prices[0].report.move.id).isEqualTo("M9")
        assertThat(cancelled.prices[0].totalInPence()).isEqualTo(101)

    }

    @Test
    fun `Summary calculated correctly`(){
        val summary = movePrices.map{it.summary}.summary()
        assertThat(summary.percentage).isEqualTo(1.0)
        assertThat(summary.volume).isEqualTo(5)
        assertThat(summary.volumeUnpriced).isEqualTo(3)
        assertThat(summary.totalPriceInPence).isEqualTo(202) // standard and cancelled move
        assertThat(summary.totalPriceInPounds).isEqualTo(2.02)
    }

}