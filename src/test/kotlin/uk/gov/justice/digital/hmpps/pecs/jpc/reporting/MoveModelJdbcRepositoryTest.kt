package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun `findAllForSupplierAndMovePriceTypeInDateRange`() {

        locationRepository.save(wyi)
        locationRepository.save(gni)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = wyi, toLocation = gni, priceInPence = 999, supplier = Supplier.SERCO))

        moveModelRepository.save(standardMoveWithoutJourneys)
        moveModelRepository.save(standardMoveWithJourneys)

        journeyModelRepository.save(journeyModel1)
        journeyModelRepository.save(journeyModel2)

        entityManager.flush()

        val moves = moveModelJdbcRepository.findAllForSupplierAndMovePriceTypeInDateRange(Supplier.SERCO, MovePriceType.STANDARD, moveDate, moveDate)

        // Move with journeys should be first
        assertThat(moves[0].moveId).isEqualTo(standardMoveWithJourneys.moveId)
        assertThat(moves[0].journeys[0].journeyId).isEqualTo(journeyModel1.journeyId)
        assertThat(moves[0].journeys[1].journeyId).isEqualTo(journeyModel2.journeyId)

        // Move without journeys should be second
        assertThat(moves[1].moveId).isEqualTo(standardMoveWithoutJourneys.moveId)

    }

    @Test
    fun `findSummaryForSupplierInDateRange`() {

        locationRepository.save(wyi)
        locationRepository.save(gni)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = wyi, toLocation = gni, priceInPence = 999, supplier = Supplier.SERCO))

        moveModelRepository.save(standardMoveWithJourneys)

        journeyModelRepository.save(journeyModel1)
        journeyModelRepository.save(journeyModel2)

        entityManager.flush()

        val summaries = moveModelJdbcRepository.findSummaryForSupplierInDateRange(Supplier.SERCO, moveDate, moveDate)
        assertThat(summaries.standard.moves[0].moveId).isEqualTo(standardMoveWithJourneys.moveId)
        assertThat(summaries.standard.summary).isEqualTo(Summary(1.0, 1, 0, 1998))

    }
}