package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class MovePersisterTest {

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Autowired
    lateinit var moveRepository: MoveRepository

    @Autowired
    lateinit var journeyRepository: JourneyRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var timeSource: TimeSource

    final val from: LocalDate = LocalDate.of(2020, 9, 1)
    final val to: LocalDate = LocalDate.of(2020, 9, 6)

    lateinit var redirectMove: Move

    lateinit var movePersister: MovePersister
    lateinit var personPersister: PersonPersister

    @BeforeEach
    fun beforeEach() {
        val fromLocation = WYIPrisonLocation()
        val toLocation = GNICourtLocation()
        locationRepository.save(fromLocation)
        locationRepository.save(toLocation)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = fromLocation, toLocation = toLocation, priceInPence = 999, supplier = Supplier.SERCO, effectiveYear = 2020))

        val journey1 = reportJourneyFactory().copy(journeyId = "J1", billable = true, vehicleRegistration = "REG1")
        val journey2 = reportJourneyFactory().copy(journeyId = "J2", billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))
        val moveRedirectEvent = moveEventFactory(eventId = "E2", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))

        journey1.events.addAll(mutableSetOf(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                journeyEventFactory(journeyEventId = "E5", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))))

        journey2.events.addAll(mutableSetOf(journeyEventFactory(journeyEventId = "E6", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                journeyEventFactory(journeyEventId = "E7", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))))

        redirectMove = reportMoveFactory(
            events = mutableSetOf(moveStartEvent, moveRedirectEvent, moveCompleteEvent),
            journeys = mutableSetOf(journey1, journey2)
        )

        movePersister = MovePersister(moveRepository, journeyRepository, eventRepository, timeSource)
        personPersister = PersonPersister(moveRepository, timeSource)
    }

    @Test
    fun `Persist redirect move`() {
        movePersister.persist(listOf(redirectMove))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        // This move should have the 3 move events
        assertThat(moveEvents(retrievedRedirectMove.moveId).map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3")

        // It should have the 2 journeys
        assertThat(journeys(retrievedRedirectMove.moveId).map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2")
    }


    @Ignore
    fun `Persist PII data`() {
        movePersister.persist(listOf(redirectMove))

        val reportPerson = reportPersonFactory().copy(profileId = defaultProfileId)
        personPersister.persist(listOf(reportPerson))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

        // PII data should be populated
        assertThat(retrievedRedirectMove.ethnicity).isEqualTo("White American")
        assertThat(retrievedRedirectMove.gender).isEqualTo("male")
        assertThat(retrievedRedirectMove.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
        assertThat(retrievedRedirectMove.firstNames).isEqualTo("Billy the")
        assertThat(retrievedRedirectMove.lastName).isEqualTo("Kid")

        // persist report again and check move date is updated and PII data left intact
        movePersister.persist(listOf(redirectMove.copy(moveDate = LocalDate.of(2020, 1, 1))))
        entityManager.flush()

        val retrievedAgainRedirectMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedAgainRedirectMove.moveDate).isEqualTo(LocalDate.of(2020, 1, 1))
        assertThat(retrievedAgainRedirectMove.lastName).isEqualTo("Kid")
    }

    @Test
    fun `Persist updated move`() {
        movePersister.persist(listOf(redirectMove))

        // persist again to check that updating it works
        movePersister.persist(listOf(redirectMove.copy(reference = "NEWREF")))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

        // The ref should be the updated ref
        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")
    }

    @Test
    fun `Persist two moves`() {
        val multiMove = reportMoveFactory(moveId = "NOJOURNEY",
                events = mutableSetOf(
                    moveEventFactory(eventId = "E8", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                    moveEventFactory(eventId = "E9", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                    moveEventFactory(eventId = "E10", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeys = mutableSetOf()
        )

        movePersister.persist(listOf(redirectMove, multiMove))
        entityManager.flush()

        assertThat(moveRepository.findAll().map { it.moveId }).containsExactlyInAnyOrder("M1", "NOJOURNEY")
    }

    @Test
    fun `Persist new event`() {
        movePersister.persist(listOf(redirectMove))

        val moveWithNewEvent = redirectMove.copy(
            events = mutableSetOf(moveEventFactory(eventId = "E400", type = EventType.MOVE_LOCKOUT.value, occurredAt = from.atStartOfDay().plusHours(10)))
        )

        movePersister.persist(listOf(moveWithNewEvent))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(moveWithNewEvent.moveId).get()

        // Previous and new events should be present
        assertThat(moveEvents(retrievedMove.moveId).map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3", "E400")
    }

    @Test
    fun `Journey with move start event in Sept 2021 persisted with 2021 effective year`() {

        redirectMove.journeys.find { it.journeyId == "J1" }?.events?.clear()
        redirectMove.journeys.toList()[0].events.add(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.plusYears(1).atStartOfDay().plusHours(5)))

        movePersister.persist(listOf(redirectMove))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(journeys(retrievedMove.moveId).find { it.journeyId == "J1" }?.effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Journey with no journey start event, with a move date in Sept 2021 is persisted with 2021 effective year`() {

        val redirectMoveWith2021Move = redirectMove.copy(
            moveDate = LocalDate.of(2021, 9, 1),
            journeys = mutableSetOf(redirectMove.journeys.toList()[0].copy(events = mutableSetOf())))

        movePersister.persist(listOf(redirectMoveWith2021Move))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(journeys(retrievedMove.moveId).toList().first().effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Persist new journey`() {
        movePersister.persist(listOf(redirectMove))

        val newJourney = reportJourneyFactory(
            journeyId = "J400",
            events = mutableSetOf(journeyEventFactory(journeyEventId = "JE400", type = EventType.JOURNEY_LODGING.value, occurredAt = from.atStartOfDay().plusHours(10))))

        val moveWithNewJourney = redirectMove.copy(
            journeys = mutableSetOf(newJourney)
        )

        movePersister.persist(listOf(moveWithNewJourney))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()

        // Previous and new journeys should be present
        assertThat(journeys(retrievedMove.moveId).map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2", "J400")
    }

    @Test
    fun `Persist non complete move then complete it`() {

        val journey1 = reportJourneyFactory().copy(
                journeyId = "J1",
                billable = true,
                vehicleRegistration = "REG1",
                events = mutableSetOf(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5))))

        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))

        val inTransitMove = reportMoveFactory(
            status = MoveStatus.in_transit,
            events = mutableSetOf(moveStartEvent),
            journeys = mutableSetOf(journey1)
        )

        movePersister.persist(listOf(inTransitMove))
        entityManager.flush()
        val retrievedInTransitMove = moveRepository.findById(inTransitMove.moveId).get()
        assertThat(retrievedInTransitMove.moveType).isNull() // should not have a move type yet - it's in_transit

        val journeyCompleteEvent = journeyEventFactory(journeyEventId = "J1", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))

        journey1.events.add(journeyCompleteEvent)
        val completedMoveWithNewJourney = inTransitMove.copy(
            status = MoveStatus.completed,
            journeys = mutableSetOf(journey1),
            events = mutableSetOf(moveCompleteEvent)
        )

        movePersister.persist(listOf(completedMoveWithNewJourney))
        entityManager.flush()

        val retrievedCompletedMove = moveRepository.findById(completedMoveWithNewJourney.moveId).get()
        assertThat(retrievedCompletedMove.moveType).isEqualTo(MoveType.STANDARD)
    }


    @Test
    fun `Cancelled billable move`(){
        val cancelledBillable = reportMoveFactory(
            moveId = "M9",
            status = MoveStatus.cancelled,
            fromLocation = fromPrisonNomisAgencyId(),
            fromLocationType = "prison",
            toLocation = toCourtNomisAgencyId(),
            toLocationType = "prison",
            cancellationReason = "cancelled_by_pmu",
            date = to,
            events= mutableSetOf(
                moveEventFactory(type = EventType.MOVE_ACCEPT.value, moveId = "M9", occurredAt = to.atStartOfDay().minusHours(24)),
                moveEventFactory(type = EventType.MOVE_CANCEL.value, moveId = "M9", occurredAt = to.atStartOfDay().minusHours(2))
            ),
        )
        movePersister.persist(listOf(cancelledBillable))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(cancelledBillable.moveId).get()
        val journeys = journeyRepository.findAll()

        assertThat(retrievedMove.moveType).isEqualTo(MoveType.CANCELLED)
        assertThat(journeys.all { it.notes!!.contains("FAKE") }).isTrue
    }

    private fun moveEvents(moveId :String ) = eventRepository.findAllByEventableId(moveId)

    private fun journeys(moveId: String) = journeyRepository.findAllByMoveId(moveId)
}

