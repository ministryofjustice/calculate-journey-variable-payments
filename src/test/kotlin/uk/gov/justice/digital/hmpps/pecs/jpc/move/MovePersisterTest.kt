package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
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
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var timeSource: TimeSource

    final val from: LocalDate = LocalDate.of(2020, 9, 1)
    final val to: LocalDate = LocalDate.of(2020, 9, 6)

    lateinit var redirectMove: Move
    lateinit var redirectReport: Report

    lateinit var movePersister: MovePersister
    lateinit var personPersister: PersonPersister

    @BeforeEach
    fun beforeEach() {
        val fromLocation = WYIPrisonLocation()
        val toLocation = GNICourtLocation()
        locationRepository.save(fromLocation)
        locationRepository.save(toLocation)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = fromLocation, toLocation = toLocation, priceInPence = 999, supplier = Supplier.SERCO, effectiveYear = 2020))

        redirectMove = reportMoveFactory()

        val journey1 = reportJourneyFactory().copy(journeyId = "J1", billable = true, vehicleRegistration = "REG1")
        val journey2 = reportJourneyFactory().copy(journeyId = "J2", billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))
        val moveRedirectEvent = moveEventFactory(eventId = "E2", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))

        journey1.events += mutableSetOf(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                journeyEventFactory(journeyEventId = "E5", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10)))

        journey2.events += mutableSetOf(journeyEventFactory(journeyEventId = "E6", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                journeyEventFactory(journeyEventId = "E7", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10)))

        redirectReport = Report(
            move = redirectMove,
            moveEvents = listOf(moveStartEvent, moveRedirectEvent, moveCompleteEvent),
            journeys = listOf(journey1, journey2)
        )

        movePersister = MovePersister(moveRepository, timeSource)
        personPersister = PersonPersister(moveRepository, timeSource)
    }

    @Test
    fun `Persist redirect move`() {
        movePersister.persist(listOf(redirectReport))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        // This move should have the 3 move events
        assertThat(retrievedRedirectMove.events.map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3")

        // It should have the 2 journeys
        assertThat(retrievedRedirectMove.journeys.map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2")
    }


    @Test
    fun `Persist PII data`() {
        movePersister.persist(listOf(redirectReport))

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
        movePersister.persist(listOf(redirectReport.copy(move = redirectMove.copy(moveDate = LocalDate.of(2020, 1, 1)))))
        entityManager.flush()

        val retrievedAgainRedirectMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedAgainRedirectMove.moveDate).isEqualTo(LocalDate.of(2020, 1, 1))
        assertThat(retrievedAgainRedirectMove.lastName).isEqualTo("Kid")
    }

    @Test
    fun `Persist updated move`() {
        movePersister.persist(listOf(redirectReport))

        // persist again to check that updating it works
        movePersister.persist(listOf(redirectReport.copy(move = redirectMove.copy(reference = "NEWREF"))))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

        // The ref should be the updated ref
        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")
    }

    @Test
    fun `Persist two moves`() {
        val noJourneyMove = reportMoveFactory(moveId = "NOJOURNEY")

        val multiReport = Report(
                move = noJourneyMove,
                moveEvents = listOf(
                    moveEventFactory(eventId = "E8", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                    moveEventFactory(eventId = "E9", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                    moveEventFactory(eventId = "E10", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeys = listOf()
        )

        movePersister.persist(listOf(redirectReport, multiReport))
        entityManager.flush()

        assertThat(moveRepository.findAll().map { it.moveId }).containsExactlyInAnyOrder("M1", "NOJOURNEY")
    }

    @Test
    fun `Persist new event`() {
        movePersister.persist(listOf(redirectReport))

        val reportWithNewEvent = Report(
            move = redirectMove,
            moveEvents = listOf(moveEventFactory(eventId = "E400", type = EventType.MOVE_LOCKOUT.value, occurredAt = from.atStartOfDay().plusHours(10))),
        )

        movePersister.persist(listOf(reportWithNewEvent))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()

        // Previous and new events should be present
        assertThat(retrievedMove.events.map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3", "E400")
    }

    @Test
    fun `Journey with move start event in Sept 2021 persisted with 2021 effective year`() {

        val redirectReportWith2021Journey = redirectReport.copy(
            journeys = listOf(redirectReport.journeys[0].copy(
                events = mutableSetOf(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.plusYears(1).atStartOfDay().plusHours(5))))))

        movePersister.persist(listOf(redirectReportWith2021Journey))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedMove.journeys.toList().first().effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Journey with no journey start event, with a move date in Sept 2021 is persisted with 2021 effective year`() {

        val redirectReportWith2021Move = redirectReport.copy(
            move = redirectReport.move.copy(moveDate = LocalDate.of(2021, 9, 1)),
            journeys = listOf(redirectReport.journeys[0].copy(events = mutableSetOf())))

        movePersister.persist(listOf(redirectReportWith2021Move))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedMove.journeys.toList().first().effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Persist new journey`() {
        movePersister.persist(listOf(redirectReport))

        val newJourney = reportJourneyFactory(
            journeyId = "J400",
            events = mutableSetOf(journeyEventFactory(journeyEventId = "JE400", type = EventType.JOURNEY_LODGING.value, occurredAt = from.atStartOfDay().plusHours(10))))

        val reportWithNewJourney = Report(
            move = redirectMove,
            journeys = listOf(newJourney),
        )

        movePersister.persist(listOf(reportWithNewJourney))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.moveId).get()

        // Previous and new journeys should be present
        assertThat(retrievedMove.journeys.map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2", "J400")
    }

    @Test
    fun `Persist non complete move then complete it`() {

        val journey1 = reportJourneyFactory().copy(
                journeyId = "J1",
                billable = true,
                vehicleRegistration = "REG1",
                events = mutableSetOf(journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5))))

        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))

        val inTransitReport = Report(
            move = reportMoveFactory(status = MoveStatus.in_transit),
            moveEvents = listOf(moveStartEvent),
            journeys = listOf(journey1)
        )

        movePersister.persist(listOf(inTransitReport))
        entityManager.flush()
        val retrievedInTransitMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedInTransitMove.moveType).isNull() // should not have a move type yet - it's in_transit

        val journeyCompleteEvent = journeyEventFactory(journeyEventId = "J1", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))

        journey1.events += journeyCompleteEvent
        val completedReportWithNewJourney = Report(
            move = inTransitReport.move.copy(status = MoveStatus.completed),
            journeys = listOf(journey1),
            moveEvents = listOf(moveCompleteEvent)
        )

        movePersister.persist(listOf(completedReportWithNewJourney))
        entityManager.flush()

        val retrievedCompletedMove = moveRepository.findById(redirectMove.moveId).get()
        assertThat(retrievedCompletedMove.moveType).isEqualTo(MoveType.STANDARD)
    }
}

