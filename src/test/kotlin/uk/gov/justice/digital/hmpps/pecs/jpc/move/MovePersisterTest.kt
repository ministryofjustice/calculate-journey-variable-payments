package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportJourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.WYIPrisonLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportJourneyEventFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportJourneyFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportMoveEventFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportMoveFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportPersonFactory
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
    lateinit var moveQueryRepository: MoveQueryRepository

    @Autowired
    lateinit var journeyRepository: JourneyRepository

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

        val redirectMove = reportMoveFactory()
        val noJourneyMove = reportMoveFactory(moveId = "NOJOURNEY")

        val journey1 = reportJourneyFactory().copy(id = "J1", billable = true, vehicleRegistration = "REG1")
        val journey2 = reportJourneyFactory().copy(id = "J2", billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val moveStartEvent = reportMoveEventFactory(eventId = "E1", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5))
        val moveRedirectEvent = reportMoveEventFactory(eventId = "E2", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7))
        val moveCompleteEvent = reportMoveEventFactory(eventId = "E3", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
        val completedRedirectMoveWithPricedBillableJourney = Report(
                move = redirectMove,
                person = reportPersonFactory(),
                moveEvents = listOf(moveStartEvent, moveRedirectEvent, moveCompleteEvent),
                journeysWithEvents = listOf(
                        ReportJourneyWithEvents(journey1, listOf(
                            reportJourneyEventFactory(journeyEventId = "E4", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                            reportJourneyEventFactory(journeyEventId = "E5", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        ),
                        ReportJourneyWithEvents(journey2, listOf(
                                reportJourneyEventFactory(journeyEventId = "E6", type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                                reportJourneyEventFactory(journeyEventId = "E7", type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        )
                )
        )

        val multiMoveBecauseNoJourney = Report(
                move = noJourneyMove,
                person = reportPersonFactory(),
                moveEvents = listOf(
                        reportMoveEventFactory(eventId = "E8", type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        reportMoveEventFactory(eventId = "E9", type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        reportMoveEventFactory(eventId = "E10", type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf()
        )

        val persister = MoveModelPersister(moveRepository, journeyRepository)
        val params = FilterParams(Supplier.SERCO, from, to)

        persister.persist(params, listOf(completedRedirectMoveWithPricedBillableJourney, multiMoveBecauseNoJourney))

        // persist again to check that updating it works
        persister.persist(params, listOf(completedRedirectMoveWithPricedBillableJourney.copy(move = redirectMove.copy(reference = "NEWREF"))))

        entityManager.flush()

        val retrievedRedirectMove = moveRepository.findById(redirectMove.id).get()

        // The ref should be the updated ref
        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")

        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        // This move should have the 3 move events
        assertThat(retrievedRedirectMove.events.map { it.id }).containsExactlyInAnyOrder("E1", "E2", "E3")

        // It should have the 2 journeys
        assertThat(retrievedRedirectMove.journeys.map { it.journeyId }).containsExactlyInAnyOrder("J1", "J2")


        val retrievedNoJourneyMove = moveRepository.findById(noJourneyMove.id).get()
        assertThat(retrievedNoJourneyMove.reference).isEqualTo("UKW4591N")
    }
}

