package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.journeyEventFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.moveEventFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.reportJourneyFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.reportMoveFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.toCourtNomisAgencyId
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.WYIPrisonLocation as WYIPrisonLocation1
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.fromPrisonNomisAgencyId as fromPrisonNomisAgencyId1

const val TOO_LONG: Int = 3000

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

  private val timeSource: TimeSource = TimeSource { LocalDateTime.now() }

  private val fromSeptember1st2020: java.time.LocalDate = java.time.LocalDate.of(2020, 9, 1)
  private val toSeptember6th2020: java.time.LocalDate = java.time.LocalDate.of(2020, 9, 6)

  lateinit var redirectMove: Move

  lateinit var movePersister: MovePersister

  @BeforeEach
  fun beforeEach() {
    val fromLocation = WYIPrisonLocation1()
    val toLocation = GNICourtLocation()
    locationRepository.save(fromLocation)
    locationRepository.save(toLocation)

    priceRepository.save(
      Price(
        id = UUID.randomUUID(),
        fromLocation = fromLocation,
        toLocation = toLocation,
        priceInPence = 999,
        supplier = Supplier.SERCO,
        effectiveYear = 2020
      )
    )

    val journey1 = reportJourneyFactory().copy(
      journeyId = "J1",
      billable = true,
      vehicleRegistration = "REG1",
      events = (reportJourneyFactory().events ?: emptyList()) +
        listOf(
          journeyEventFactory(
            journeyEventId = "E4",
            type = EventType.JOURNEY_START.value,
            occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
          ),
          journeyEventFactory(
            journeyEventId = "E5",
            type = EventType.JOURNEY_COMPLETE.value,
            occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
          )
        )
    )

    val journey2 = reportJourneyFactory().copy(
      journeyId = "J2",
      billable = true,
      fromNomisAgencyId = "NOT_MAPPED",
      vehicleRegistration = "REG2",
      events = (reportJourneyFactory().events ?: emptyList()) +
        listOf(
          journeyEventFactory(
            journeyEventId = "E6",
            type = EventType.JOURNEY_START.value,
            occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
          ),
          journeyEventFactory(
            journeyEventId = "E7",
            type = EventType.JOURNEY_COMPLETE.value,
            occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
          )
        )
    )

    val moveStartEvent =
      moveEventFactory(
        eventId = "E1",
        type = EventType.MOVE_START.value,
        occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
      )
    val moveRedirectEvent = moveEventFactory(
      eventId = "E2",
      type = EventType.MOVE_REDIRECT.value,
      notes = "This was redirected.",
      occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(7)
    )
    val moveCompleteEvent = moveEventFactory(
      eventId = "E3",
      type = EventType.MOVE_COMPLETE.value,
      occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
    )

    redirectMove = reportMoveFactory(
      events = listOf(moveStartEvent, moveRedirectEvent, moveCompleteEvent),
      journeys = listOf(journey1, journey2)
    )

    movePersister = MovePersister(moveRepository, journeyRepository, eventRepository, timeSource)
  }

  @Test
  fun `Persist redirect move`() {
    movePersister.persist(listOf(redirectMove))

    entityManager.flush()
    val retrievedRedirectMove = moveRepository.findById(redirectMove.moveId).get()

    assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")

    // This move should have the 3 move events
    assertThat(moveEvents(retrievedRedirectMove.moveId).map { it.eventId }).containsExactlyInAnyOrder("E1", "E2", "E3")

    // It should have the 2 journeys
    assertThat(journeys(retrievedRedirectMove.moveId).map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2")
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
    val multiMove = reportMoveFactory(
      moveId = "NOJOURNEY",
      events = listOf(
        moveEventFactory(
          eventId = "E8",
          type = EventType.MOVE_START.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
        ),
        moveEventFactory(
          eventId = "E9",
          type = EventType.MOVE_REDIRECT.value,
          notes = "This was redirected.",
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(7)
        ),
        moveEventFactory(
          eventId = "E10",
          type = EventType.MOVE_COMPLETE.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
        )
      ),
      journeys = listOf()
    )

    movePersister.persist(listOf(redirectMove, multiMove))
    entityManager.flush()

    assertThat(moveRepository.findAll().map { it.moveId }).containsExactlyInAnyOrder("M1", "NOJOURNEY")
  }

  @Test
  fun `Persist one out of two moves due to field value too long`() {
    val multiMove = reportMoveFactory(
      moveId = "NOJOURNEY",
      events = listOf(
        moveEventFactory(
          eventId = "E8",
          type = EventType.MOVE_START.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
        ),
        moveEventFactory(
          eventId = "E9",
          type = EventType.MOVE_REDIRECT.value,
          notes = "This was redirected.",
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(7)
        ),
        moveEventFactory(
          eventId = "E10",
          type = EventType.MOVE_COMPLETE.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
        )
      ),
      journeys = listOf()
    )

    movePersister.persist(listOf(redirectMove.copy(cancellationReasonComment = "a".repeat(TOO_LONG)), multiMove))
    entityManager.flush()

    assertThat(moveRepository.findAll().map { it.moveId }).containsOnly("NOJOURNEY")
  }

  @Test
  fun `Persist new event`() {
    movePersister.persist(listOf(redirectMove))

    val moveWithNewEvent = redirectMove.copy(
      events = listOf(
        moveEventFactory(
          eventId = "E400",
          type = EventType.MOVE_LOCKOUT.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
        )
      )
    )

    movePersister.persist(listOf(moveWithNewEvent))
    entityManager.flush()

    val retrievedMove = moveRepository.findById(moveWithNewEvent.moveId).get()

    // Previous and new events should be present
    assertThat(moveEvents(retrievedMove.moveId).map { it.eventId }).containsExactlyInAnyOrder("E1", "E2", "E3", "E400")
  }

  @Test
  fun `Journey with move start event in Sept 2020 persisted with 2020 effective year`() {

    val journey = reportJourneyFactory().copy(
      events = listOf(
        journeyEventFactory(
          journeyEventId = "E4",
          type = EventType.JOURNEY_START.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
        )
      )
    )
    val moveToPersist = redirectMove.copy(journeys = listOf(journey))

    movePersister.persist(listOf(moveToPersist))
    entityManager.flush()

    val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
    assertThat(journeys(retrievedMove.moveId).find { it.journeyId == "J1" }?.effectiveYear).isEqualTo(2020)
  }

  @Test
  fun `Journey with move start event in Aug 31st 2021 persisted with 2020 effective year`() {

    val journey = reportJourneyFactory().copy(
      events = listOf(
        journeyEventFactory(
          journeyEventId = "E4",
          type = EventType.JOURNEY_START.value,
          occurredAt = LocalDate.of(2021, 8, 31).atStartOfDay().plusHours(5)
        )
      )
    )
    val moveToPersist = redirectMove.copy(journeys = listOf(journey))

    movePersister.persist(listOf(moveToPersist))
    entityManager.flush()

    val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
    assertThat(journeys(retrievedMove.moveId).find { it.journeyId == "J1" }?.effectiveYear).isEqualTo(2020)
  }

  @Test
  fun `Journey with move start event in Sept 2021 persisted with 2021 effective year`() {

    val journey = reportJourneyFactory().copy(
      events = listOf(
        journeyEventFactory(
          journeyEventId = "E4",
          type = EventType.JOURNEY_START.value,
          occurredAt = fromSeptember1st2020.plusYears(1).atStartOfDay().plusHours(5)
        )
      )
    )
    val moveToPersist = redirectMove.copy(journeys = listOf(journey))

    movePersister.persist(listOf(moveToPersist))
    entityManager.flush()

    val retrievedMove = moveRepository.findById(redirectMove.moveId).get()
    assertThat(journeys(retrievedMove.moveId).find { it.journeyId == "J1" }?.effectiveYear).isEqualTo(2021)
  }

  @Test
  fun `Journey with no journey start event, with a move date in Sept 2021 is persisted with 2021 effective year`() {

    val redirectMoveWith2021Move = redirectMove.copy(
      moveDate = java.time.LocalDate.of(2021, 9, 1),
      journeys = listOf(redirectMove.journeys.toList()[0].copy(events = listOf()))
    )

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
      events = listOf(
        journeyEventFactory(
          journeyEventId = "JE400",
          type = EventType.JOURNEY_LODGING.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
        )
      )
    )

    val moveWithNewJourney = redirectMove.copy(
      journeys = listOf(newJourney)
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
      events = listOf(
        journeyEventFactory(
          journeyEventId = "E4",
          type = EventType.JOURNEY_START.value,
          occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
        )
      )
    )

    val moveStartEvent =
      moveEventFactory(
        eventId = "E1",
        type = EventType.MOVE_START.value,
        occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(5)
      )

    val inTransitMove = reportMoveFactory(
      status = MoveStatus.in_transit,
      events = listOf(moveStartEvent),
      journeys = listOf(journey1)
    )

    movePersister.persist(listOf(inTransitMove))
    entityManager.flush()
    val retrievedInTransitMove = moveRepository.findById(inTransitMove.moveId).get()
    assertThat(retrievedInTransitMove.moveType).isNull() // should not have a move type yet - it's in_transit

    val journeyCompleteEvent = journeyEventFactory(
      journeyEventId = "J1",
      type = EventType.JOURNEY_COMPLETE.value,
      occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
    )
    val moveCompleteEvent = moveEventFactory(
      eventId = "E3",
      type = EventType.MOVE_COMPLETE.value,
      occurredAt = fromSeptember1st2020.atStartOfDay().plusHours(10)
    )

    val journeyWithMoveCompleteEvent = journey1.copy(events = (journey1.events ?: emptyList()) + journeyCompleteEvent)
    val completedMoveWithNewJourney = inTransitMove.copy(
      status = MoveStatus.completed,
      journeys = listOf(journeyWithMoveCompleteEvent),
      events = listOf(moveCompleteEvent)
    )

    movePersister.persist(listOf(completedMoveWithNewJourney))
    entityManager.flush()

    val retrievedCompletedMove = moveRepository.findById(completedMoveWithNewJourney.moveId).get()
    assertThat(retrievedCompletedMove.moveType).isEqualTo(MoveType.STANDARD)
  }

  @Test
  fun `Cancelled billable move`() {
    val cancelledBillable = reportMoveFactory(
      moveId = "M9",
      status = MoveStatus.cancelled,
      fromLocation = fromPrisonNomisAgencyId1(),
      fromLocationType = "prison",
      toLocation = toCourtNomisAgencyId(),
      toLocationType = "prison",
      cancellationReason = "cancelled_by_pmu",
      date = toSeptember6th2020,
      events = listOf(
        moveEventFactory(
          eventId = "E1",
          type = EventType.MOVE_ACCEPT.value,
          moveId = "M9",
          occurredAt = toSeptember6th2020.atStartOfDay().minusHours(24)
        ),
        moveEventFactory(
          eventId = "E2",
          type = EventType.MOVE_CANCEL.value,
          moveId = "M9",
          occurredAt = toSeptember6th2020.atStartOfDay().minusHours(2)
        )
      ),
    )
    movePersister.persist(listOf(cancelledBillable))
    entityManager.flush()

    val retrievedMove = moveRepository.findById(cancelledBillable.moveId).get()
    val journeys = journeyRepository.findAll()

    assertThat(retrievedMove.moveType).isEqualTo(MoveType.CANCELLED)
    assertThat(journeys.all { it.notes!!.contains("FAKE") }).isTrue
  }

  @Test
  fun `fake journey effective year taken from time move date when present`() {
    val move = moveM1()
    val fake = movePersister.fakeCancelledJourneyForPricing(move)

    assertThat(fake.effectiveYear).isEqualTo(effectiveYearForDate(move.moveDate!!))
  }

  @Test
  fun `fake journey effective year taken defaults when move date is not present`() {
    val fake = movePersister.fakeCancelledJourneyForPricing(moveM1().copy(moveDate = null))

    assertThat(fake.effectiveYear).isEqualTo(effectiveYearForDate(timeSource.date()))
  }

  private fun moveEvents(moveId: String) = eventRepository.findAllByEventableId(moveId)

  private fun journeys(moveId: String) = journeyRepository.findAllByMoveId(moveId)
}
