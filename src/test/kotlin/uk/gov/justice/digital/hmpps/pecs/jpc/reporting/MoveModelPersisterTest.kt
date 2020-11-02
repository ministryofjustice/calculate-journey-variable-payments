package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class MoveModelPersisterTest {

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

    val from = LocalDate.of(2020, 9, 1)
    val to = LocalDate.of(2020, 9, 6)


    @Test
    fun `Persist one move with one extant location and one journey`() {

        val fromLocation = WYIPrisonLocation()
        val toLocation = GNICourtLocation()

        locationRepository.save(fromLocation)
        locationRepository.save(toLocation)

        priceRepository.save(Price(id= UUID.randomUUID(), fromLocation = fromLocation, toLocation = toLocation, priceInPence = 999, supplier = Supplier.SERCO))

        val redirectMove = moveFactory()
        val noJourneyMove = moveFactory(moveId = "NOJOURNEY")

        val journey1 = journeyFactory().copy(id = UUID.randomUUID().toString(), billable = true, vehicleRegistration = "REG1")
        val journey2 = journeyFactory().copy(id = UUID.randomUUID().toString(), billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val completedMoveWithPricedBillableJourney = Report(
                move = redirectMove,
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(
                        JourneyWithEvents(journey1, listOf(
                            journeyEventFactory(type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                            journeyEventFactory(type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        ),
                        JourneyWithEvents(journey2, listOf(
                                journeyEventFactory(type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                                journeyEventFactory(type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        )
                )
        )

        val multiMoveBecauseNoJourney = Report(
                move = noJourneyMove,
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf()
        )

        val persister = MoveModelPersister(moveModelRepository, journeyModelRepository)
        val params = FilterParams(Supplier.SERCO, from, to)

        persister.persist(params, listOf(completedMoveWithPricedBillableJourney, multiMoveBecauseNoJourney))

        // persist again to check this works
        persister.persist(params, listOf(completedMoveWithPricedBillableJourney.copy(move = redirectMove.copy(reference = "NEWREF"))))

        entityManager.flush()

        val retrievedRedirectMove = moveModelRepository.findById(redirectMove.id).get()

        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")
        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        val retrievedNoJourneyMove = moveModelRepository.findById(noJourneyMove.id).get()
        assertThat(retrievedNoJourneyMove.reference).isEqualTo("UKW4591N")
    }
}

