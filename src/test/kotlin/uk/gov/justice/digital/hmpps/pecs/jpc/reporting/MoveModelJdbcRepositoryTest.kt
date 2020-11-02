package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

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
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class MoveModelJdbcRepositoryTest {

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Autowired
    lateinit var moveModelRepository: MoveModelRepository

    @Autowired
    lateinit var moveModelJdbcRepository: MoveModelJdbcRepository

    @Autowired
    lateinit var journeyModelRepository: JourneyModelRepository


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

        moveModelRepository.save(standardMoveWithJourneys)

        journeyModelRepository.save(journeyModel1)
        journeyModelRepository.save(journeyModel2)

        entityManager.flush()

        val move = moveModelJdbcRepository.findAllForSupplierAndMovePriceTypeInDateRange(Supplier.SERCO, MovePriceType.STANDARD, moveDate, moveDate)[0]

        // Move should be priced
        assertTrue(move.hasPrice())
    }

    @Test
    fun `findAllForSupplierAndMovePriceTypeInDateRange a with non billable journey`() {


        moveModelRepository.save(standardMoveWithJourneys)

        val nonBillableJourney = journeyModel(journeyId = "J3", billable = false)
        val journeyWithoutDropOffDate = journeyModel(journeyId = "J4", pickUpDateTime = null, dropOffDateTime = null)

        journeyModelRepository.save(journeyModel1)
        journeyModelRepository.save(journeyModel2)
        journeyModelRepository.save(nonBillableJourney)
        journeyModelRepository.save(journeyWithoutDropOffDate)

        entityManager.flush()

        val move = moveModelJdbcRepository.findAllForSupplierAndMovePriceTypeInDateRange(Supplier.SERCO, MovePriceType.STANDARD, moveDate, moveDate)[0]

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

        moveModelRepository.save(standardMoveWithJourneys)

        journeyModelRepository.save(journeyModel1)
        journeyModelRepository.save(journeyModel2)

        entityManager.flush()

        val summaries = moveModelJdbcRepository.findSummaryForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(summaries.standard.moves[0].moveId).isEqualTo(standardMoveWithJourneys.moveId)
        assertThat(summaries.standard.summary).isEqualTo(Summary(1.0, 1, 0, 1998))

    }
}