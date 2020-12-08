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

    final val from: LocalDate = LocalDate.of(2020, 9, 1)
    final val to: LocalDate = LocalDate.of(2020, 9, 6)

    lateinit var redirectMove: ReportMove
    lateinit var redirectReport: Report

    lateinit var persister: MovePersister

    @BeforeEach
    fun beforeEach() {
        val fromLocation = WYIPrisonLocation()
        val toLocation = GNICourtLocation()
        locationRepository.save(fromLocation)
        locationRepository.save(toLocation)

        priceRepository.save(Price(id = UUID.randomUUID(), fromLocation = fromLocation, toLocation = toLocation, priceInPence = 999, supplier = Supplier.SERCO, effectiveYear = 2020))

        redirectMove = reportMoveFactory()

        val journey1 = reportJourneyFactory().copy(id = "J1", billable = true, vehicleRegistration = "REG1")
        val journey2 = reportJourneyFactory().copy(id = "J2", billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))
        val moveRedirectEvent = moveEventFactory(eventId = "E2", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
        redirectReport = Report(
                move = redirectMove,
                person = personFactory(),
                moveEvents = listOf(moveStartEvent, moveRedirectEvent, moveCompleteEvent),
                journeysWithEvents = listOf(
                        ReportJourneyWithEvents(journey1, listOf(
                                journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                                journeyEventFactory(journeyEventId = "E5", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                        )
                        ),
                        ReportJourneyWithEvents(journey2, listOf(
                                journeyEventFactory(journeyEventId = "E6", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                                journeyEventFactory(journeyEventId = "E7", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                        )
                        )
                )
        )

        persister = MovePersister(moveRepository)
    }


    @Test
    fun `Persist redirect move`() {
        persister.persist(listOf(redirectReport))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.id).get()

        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        // This move should have the 3 move events
        assertThat(retrievedRedirectMove.events.map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3")

        // It should have the 2 journeys
        assertThat(retrievedRedirectMove.journeys.map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2")

        // PII data should be populated
        assertThat(retrievedRedirectMove.ethnicity).isEqualTo("White American")
        assertThat(retrievedRedirectMove.gender).isEqualTo("male")
        assertThat(retrievedRedirectMove.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
        assertThat(retrievedRedirectMove.firstNames).isEqualTo("Billy the")
        assertThat(retrievedRedirectMove.lastName).isEqualTo("Kid")


    }

    @Test
    fun `Persist updated move`() {
        persister.persist(listOf(redirectReport))

        // persist again to check that updating it works
        persister.persist(listOf(redirectReport.copy(move = redirectMove.copy(reference = "NEWREF"))))

        entityManager.flush()
        val retrievedRedirectMove = moveRepository.findById(redirectMove.id).get()

        // The ref should be the updated ref
        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")
    }

    @Test
    fun `Persist two moves`() {
        val noJourneyMove = reportMoveFactory(moveId = "NOJOURNEY")

        val multiReport = Report(
                move = noJourneyMove,
                person = personFactory(),
                moveEvents = listOf(
                        moveEventFactory(eventId = "E8", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        moveEventFactory(eventId = "E9", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        moveEventFactory(eventId = "E10", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf()
        )

        persister.persist(listOf(redirectReport, multiReport))
        entityManager.flush()

        assertThat(moveRepository.findAll().map { it.moveId }).containsExactlyInAnyOrder("M1", "NOJOURNEY")
    }

    @Test
    fun `Persist new event`() {
        persister.persist(listOf(redirectReport))

        val reportWithNewEvent = Report(
                move = redirectMove,
                moveEvents = listOf(moveEventFactory(eventId = "E400", type = EventType.MOVE_LOCKOUT.value, occurredAt = from.atStartOfDay().plusHours(10))),
                person = null
        )

        persister.persist(listOf(reportWithNewEvent))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.id).get()

        // Previous and new events should be present
        assertThat(retrievedMove.events.map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3", "E400")
    }

    @Test
    fun `Journey with move start event in Sept 2021 persisted with 2021 effective year`() {

        val redirectReportWith2021Journey = redirectReport.copy(journeysWithEvents = listOf(redirectReport.journeysWithEvents[0].copy(events = listOf(
                        journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.plusYears(1).atStartOfDay().plusHours(5))))))

        persister.persist(listOf(redirectReportWith2021Journey))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.id).get()
        assertThat(retrievedMove.journeys.toList().first().effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Journey with no journey start event, with a move date in Sept 2021 is persisted with 2021 effective year`() {

        val redirectReportWith2021Move = redirectReport.copy(move = redirectReport.move.copy(moveDate = LocalDate.of(2021, 9, 1)),
                journeysWithEvents = listOf(redirectReport.journeysWithEvents[0].copy(events =listOf())))

        persister.persist(listOf(redirectReportWith2021Move))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.id).get()
        assertThat(retrievedMove.journeys.toList().first().effectiveYear).isEqualTo(2021)
    }

    @Test
    fun `Persist new journey`() {
        persister.persist(listOf(redirectReport))

        val newJourney = reportJourneyFactory(journeyId = "J400")
        val journeyEvents = listOf(
                journeyEventFactory(journeyEventId = "JE400", type = EventType.JOURNEY_LODGING.value, occurredAt = from.atStartOfDay().plusHours(10)))

        val reportWithNewJourney = Report(
                move = redirectMove,
                journeysWithEvents = listOf(ReportJourneyWithEvents(newJourney, journeyEvents)),
                person = null
        )

        persister.persist(listOf(reportWithNewJourney))
        entityManager.flush()

        val retrievedMove = moveRepository.findById(redirectMove.id).get()

        // Previous and new journeys should be present
        assertThat(retrievedMove.journeys.map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2", "J400")
    }

    @Test
    fun `Persist non complete move then complete it`() {

        val journey1 = reportJourneyFactory().copy(id = "J1", billable = true, vehicleRegistration = "REG1")
        val moveStartEvent = moveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))

        val inTransitReport = Report(
                move = reportMoveFactory(status = MoveStatus.IN_TRANSIT.name),
                person = personFactory(),
                moveEvents = listOf(moveStartEvent),
                journeysWithEvents = listOf(
                        ReportJourneyWithEvents(journey1, listOf(
                                journeyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5))
                        )
                        )
                )
        )

        persister.persist(listOf(inTransitReport))
        entityManager.flush()
        val retrievedInTransitMove = moveRepository.findById(redirectMove.id).get()
        assertThat(retrievedInTransitMove.moveType).isNull() // should not have a move type yet - it's in_transit

        val journeyCompleteEvent = journeyEventFactory(journeyEventId = "J1", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
        val moveCompleteEvent = moveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))

        val completedReportWithNewJourney = Report(
                move = inTransitReport.move.copy(status = MoveStatus.COMPLETED.name),
                journeysWithEvents = listOf(ReportJourneyWithEvents(journey1, listOf(journeyCompleteEvent))),
                person = null,
                moveEvents = listOf(moveCompleteEvent)
        )

        persister.persist(listOf(completedReportWithNewJourney))
        entityManager.flush()

        val retrievedCompletedMove = moveRepository.findById(redirectMove.id).get()
        assertThat(retrievedCompletedMove.moveType).isEqualTo(MoveType.STANDARD)
    }
}

