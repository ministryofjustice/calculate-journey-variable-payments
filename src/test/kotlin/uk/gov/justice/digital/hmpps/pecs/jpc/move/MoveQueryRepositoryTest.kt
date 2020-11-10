package uk.gov.justice.digital.hmpps.pecs.jpc.move

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.WYIPrisonLocation
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class MoveQueryRepositoryTest {

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Autowired
    lateinit var moveRepository: MoveRepository

    @Autowired
    lateinit var moveQueryRepository: MoveQueryRepository

    @Autowired
    lateinit var journeyRepository: JourneyRepository


    @Autowired
    lateinit var entityManager: TestEntityManager

    val wyi = WYIPrisonLocation()
    val gni = GNICourtLocation()

    val standardMove = move( dropOffOrCancelledDateTime = moveDate.atStartOfDay().plusHours(5)) // should appear before the one above
    val journeyModel1 = journey()
    val journeyModel2 = journey(journeyId = "J2")

    @BeforeEach
    fun beforeEach(){
        locationRepository.save(wyi)
        locationRepository.save(gni)
        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = wyi, toLocation = gni, priceInPence = 999, supplier = Supplier.SERCO))

        moveRepository.save(standardMove)
        journeyRepository.save(journeyModel1)
        journeyRepository.save(journeyModel2)

        entityManager.flush()
    }


    @Test
    fun `move should be priced if all journeys are billable`() {
        val move = moveQueryRepository.allForSupplierAndMoveTypeInDateRange(Supplier.SERCO, MoveType.STANDARD, moveDate, moveDate)[0]

        // Move should be priced
        assertTrue(move.hasPrice())
    }

    @Test
    fun `findAllForSupplierAndMovePriceTypeInDateRange a with non billable journey`() {

        val nonBillableJourney = journey(journeyId = "J3", billable = false)
        val journeyWithoutDropOffDate = journey(journeyId = "J4", pickUpDateTime = null, dropOffDateTime = null)

        journeyRepository.save(nonBillableJourney)
        journeyRepository.save(journeyWithoutDropOffDate)

        entityManager.flush()

        val move = moveQueryRepository.allForSupplierAndMoveTypeInDateRange(Supplier.SERCO, MoveType.STANDARD, moveDate, moveDate)[0]

        assertThat(move.journeys.size).isEqualTo(4)

        // Journey 1 should have a price because it's billable
        assertTrue(move.journeys.find { it.journeyId == "J1" }!!.hasPrice())

        // Journey 3 should not have a price because it's not billable
        assertFalse(move.journeys.find { it.journeyId == "J3" }!!.hasPrice())


       // Move should not be priced if one or more journeys is not priced
        assertFalse(move.hasPrice())
    }

    @Test
    fun `all summaries`() {

        val moveWithUnbillableJourney = standardMove.copy(moveId = "M2")
        val journey3 = journey(moveId = "M2", journeyId = "J3", billable = false)

        val moveWithUnpricedJourney = standardMove.copy(moveId = "M3")
        val journey4 = journey(moveId = "M3", journeyId = "J4", billable = true, fromNomisAgencyId = "UNPRICED")

        val moveWithoutJourneys = standardMove.copy(moveId = "M4")

        moveRepository.save(moveWithUnbillableJourney)
        moveRepository.save(moveWithUnpricedJourney)
        moveRepository.save(moveWithoutJourneys)

        journeyRepository.save(journey3)
        journeyRepository.save(journey4)

        entityManager.flush()

        // The moves with no journeys, unbillable journey and unpriced journey should come out as unpried
        val summaries = moveQueryRepository.summariesForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate, 4)
        assertThat(summaries).containsExactly(MovesSummary(MoveType.STANDARD, 1.0, 4, 2, 1998))
    }


    @Test
    fun `unique journeys and journey summaries`() {

        val locationX  = Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "locationX", siteName = "banana")
        val locationY  = Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "locationY", siteName = "apple")

        locationRepository.save(locationX)
        locationRepository.save(locationY)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = wyi, toLocation = locationX, priceInPence = 201, supplier = Supplier.SERCO))

        val moveWithUnbillableJourney = standardMove.copy(moveId = "M2")
        val journey3 = journey(moveId = "M2", journeyId = "J3", billable = false, toNomisAgencyId = locationX.nomisAgencyId)

        val moveWithUnmappedLocation = standardMove.copy(moveId = "M3")
        val journey4 = journey(moveId = "M3", journeyId = "J4", billable = true, fromNomisAgencyId = "unmappedNomisAgencyId")

        val moveWithUpricedLocation = standardMove.copy(moveId = "M4")
        val journey5 = journey(moveId = "M4", journeyId = "J5", billable = true, fromNomisAgencyId = locationY.nomisAgencyId)

        moveRepository.save(moveWithUnbillableJourney) // not unpriced just because journey is not billable
        moveRepository.save(moveWithUpricedLocation) // unpriced but has location
        moveRepository.save(moveWithUnmappedLocation) // unpriced since it has no mapped location

        journeyRepository.save(journey3)
        journeyRepository.save(journey4)
        journeyRepository.save(journey5)

        entityManager.flush()

        val summaries = moveQueryRepository.journeysSummaryForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(summaries).isEqualTo(JourneysSummary(4, 1998, 1, 2))

        val uniqueJourneys = moveQueryRepository.uniqueJourneysForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(uniqueJourneys.size).isEqualTo(4)

        // Ordered by unmapped from locations first
        assertThat(uniqueJourneys[0].fromNomisAgencyId).isEqualTo("unmappedNomisAgencyId")

        // this journey should have a volume of 2
        assertThat(uniqueJourneys.find { it.fromNomisAgencyId == journeyModel1.fromNomisAgencyId }!!.volume).isEqualTo(2)
    }

    @Test
    fun `moves count`() {
        val movesCount = moveQueryRepository.countForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(movesCount).isEqualTo(1)
    }

    @Test
    fun `paging`(){
        val moves = moveQueryRepository.allForSupplierAndMoveTypeInDateRange(Supplier.SERCO, MoveType.STANDARD, moveDate, moveDate)
        val pageNo = 0
        val pageSize = 50

        val paging: Pageable = PageRequest.of(pageNo, pageSize)
        val movesPage = MovesPage(moves, paging, 1)
        println(movesPage)
    }
}