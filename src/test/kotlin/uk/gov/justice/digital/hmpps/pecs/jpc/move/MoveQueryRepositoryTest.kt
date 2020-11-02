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
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.report.WYIPrisonLocation
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

    val standardMoveWithoutJourneys = moveModel(moveId = "NOJOURNEYS", dropOffOrCancelledDateTime = moveDate.atStartOfDay().plusHours(20))
    val standardMoveWithJourneys = moveModel( dropOffOrCancelledDateTime = moveDate.atStartOfDay().plusHours(5)) // should appear before the one above
    val journeyModel1 = journeyModel()
    val journeyModel2 = journeyModel(journeyId = "J2")

    @BeforeEach
    fun beforeEach(){

        locationRepository.save(wyi)
        locationRepository.save(gni)
        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = wyi, toLocation = gni, priceInPence = 999, supplier = Supplier.SERCO))
    }


    @Test
    fun `move should be priced if all journeys are billable`() {

        moveRepository.save(standardMoveWithJourneys)

        journeyRepository.save(journeyModel1)
        journeyRepository.save(journeyModel2)

        entityManager.flush()

        val move = moveQueryRepository.findAllForSupplierAndMoveTypeInDateRange(Supplier.SERCO, MoveType.STANDARD, moveDate, moveDate)[0]

        // Move should be priced
        assertTrue(move.hasPrice())
    }

    @Test
    fun `findAllForSupplierAndMovePriceTypeInDateRange a with non billable journey`() {


        moveRepository.save(standardMoveWithJourneys)

        val nonBillableJourney = journeyModel(journeyId = "J3", billable = false)
        val journeyWithoutDropOffDate = journeyModel(journeyId = "J4", pickUpDateTime = null, dropOffDateTime = null)

        journeyRepository.save(journeyModel1)
        journeyRepository.save(journeyModel2)
        journeyRepository.save(nonBillableJourney)
        journeyRepository.save(journeyWithoutDropOffDate)

        entityManager.flush()

        val move = moveQueryRepository.findAllForSupplierAndMoveTypeInDateRange(Supplier.SERCO, MoveType.STANDARD, moveDate, moveDate)[0]

        assertThat(move.journeys.size).isEqualTo(4)

        // Journey 1 should have a price because it's billable
        assertTrue(move.journeys[0].hasPrice())

        // Journey 3 should not have a price because it's not billable
        assertFalse(move.journeys[2].hasPrice())


       // Move should not be priced if one or more journeys is not priced
        assertFalse(move.hasPrice())
    }

    @Test
    fun `findSummaryForSupplierInDateRange`() {

        moveRepository.save(standardMoveWithJourneys)

        journeyRepository.save(journeyModel1)
        journeyRepository.save(journeyModel2)

        entityManager.flush()

        val summaries = moveQueryRepository.findSummaryForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(summaries.standard.moves[0].moveId).isEqualTo(standardMoveWithJourneys.moveId)
        assertThat(summaries.standard.summary).isEqualTo(Summary(1.0, 1, 0, 1998))

    }
}