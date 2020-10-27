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

        val move = moveFactory().copy(id = UUID.randomUUID().toString())
        val journey1 = journeyFactory().copy(id = UUID.randomUUID().toString(), billable = true)
        val journey2 = journeyFactory().copy(id = UUID.randomUUID().toString(), billable = true, fromNomisAgencyId = "NOT_MAPPED")

        val completedMoveWithPricedBillableJourney = Report(
                move = move,
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

        val persister = MoveModelPersister(moveModelRepository, journeyModelRepository)
        val params = FilterParams(Supplier.SERCO, from, to)

        persister.persist(params, listOf(completedMoveWithPricedBillableJourney))

        // persist again to check this works
        persister.persist(params, listOf(completedMoveWithPricedBillableJourney.copy(move = move.copy(reference = "NEWREF"))))

        entityManager.flush()

        val retrieved = moveModelRepository.findById(move.id)

        assertThat(retrieved.get().reference).isEqualTo("NEWREF")
        assertThat(retrieved.get().notes).isEqualTo("MoveRedirect: This was redirected.")
    }
}

