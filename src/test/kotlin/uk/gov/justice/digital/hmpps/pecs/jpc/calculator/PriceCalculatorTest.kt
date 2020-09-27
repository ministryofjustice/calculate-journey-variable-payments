package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*

@ActiveProfiles("test")
@DataJpaTest
internal class PriceCalculatorTest{

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Test
    fun `Standard move priced correctly`() {
        val completedMoveWithPricedJourney = MovePersonJourneysEvents(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true), listOf()))
        )

        val completedMoveWithUnpricedJourney = MovePersonJourneysEvents(
                move = moveFactory(moveId = "M2", fromLocation = "NOTPRICED"),
                person = personFactory(),
                events = listOf(),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(billable = true, fromLocation = "NOTPRICED"), listOf()))
        )
        val cancelledMove = MovePersonJourneysEvents(
                move = moveFactory(moveId = "M3", status = "cancelled"),
                person = personFactory(),
                events = listOf(),
                journeysWithEvents = listOf(JourneyWithEvents(journeyFactory(journeyId = "J2"), listOf()))
        )

        // Set up correct location
        locationRepository.save(Location(LocationType.PR, "WYI", "from"))
        locationRepository.save(Location(LocationType.PR, "GNI", "to"))
        entityManager.flush()

        val from = locationRepository.findBySiteName("from")
        val to = locationRepository.findBySiteName("to")

        // Set up a price
        priceRepository.save(Price(
                journeyId = 1,
                supplier = Supplier.SERCO,
                fromLocationName = from!!.siteName,
                fromLocationId = from.id,
                toLocationName = to!!.siteName,
                toLocationId = to.id,
                priceInPence = 101
            )
        )

        entityManager.flush()


        val prices = PriceCalculator(locationRepository, priceRepository).standardPrices(
                Supplier.SERCO,
                listOf(completedMoveWithPricedJourney, completedMoveWithUnpricedJourney, cancelledMove)
        )

        // We should have 2 prices for the 2 standard moves, one with prices and one without
        Assertions.assertEquals(listOf("M1", "M2"), prices.map { it.movePersonJourneysEvents.move.id })

        // M1 should be 101p
        Assertions.assertEquals(101, prices[0].journeyPrices.first().priceInPence)

        // M2 price should not be set
        Assertions.assertNull(prices[1].journeyPrices.first().priceInPence)
    }
}