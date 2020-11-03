package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.ReportJourneyWithEvents
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.report.*
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

        val journey1 = reportJourneyFactory().copy(id = UUID.randomUUID().toString(), billable = true, vehicleRegistration = "REG1")
        val journey2 = reportJourneyFactory().copy(id = UUID.randomUUID().toString(), billable = true, fromNomisAgencyId = "NOT_MAPPED", vehicleRegistration = "REG2")

        val completedMoveWithPricedBillableJourney = Report(
                reportMove = redirectMove,
                reportPerson = reportPersonFactory(),
                reportEvents = listOf(
                        reportMoveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        reportMoveEventFactory(type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        reportMoveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEventReports = listOf(
                        ReportJourneyWithEvents(journey1, listOf(
                            reportJourneyEventFactory(type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                            reportJourneyEventFactory(type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        ),
                        ReportJourneyWithEvents(journey2, listOf(
                                reportJourneyEventFactory(type = EventType.JOURNEY_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                                reportJourneyEventFactory(type = EventType.JOURNEY_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                            )
                        )
                )
        )

        val multiMoveBecauseNoJourney = Report(
                reportMove = noJourneyMove,
                reportPerson = reportPersonFactory(),
                reportEvents = listOf(
                        reportMoveEventFactory(type = EventType.MOVE_START.value, occurredAt = from.atStartOfDay().plusHours(5)),
                        reportMoveEventFactory(type = EventType.MOVE_REDIRECT.value, notes = "This was redirected.", occurredAt = from.atStartOfDay().plusHours(7)),
                        reportMoveEventFactory(type = EventType.MOVE_COMPLETE.value, occurredAt = from.atStartOfDay().plusHours(10))
                ),
                journeysWithEventReports = listOf()
        )

        val persister = MoveModelPersister(moveRepository, journeyRepository)
        val params = FilterParams(Supplier.SERCO, from, to)

        persister.persist(params, listOf(completedMoveWithPricedBillableJourney, multiMoveBecauseNoJourney))

        // persist again to check this works
        persister.persist(params, listOf(completedMoveWithPricedBillableJourney.copy(reportMove = redirectMove.copy(reference = "NEWREF"))))

        entityManager.flush()

        val retrievedRedirectMove = moveRepository.findById(redirectMove.id).get()

        assertThat(retrievedRedirectMove.reference).isEqualTo("NEWREF")
        assertThat(retrievedRedirectMove.notes).isEqualTo("MoveRedirect: This was redirected.")
        assertThat(retrievedRedirectMove.vehicleRegistration).isEqualTo("REG1, REG2")

        val retrievedNoJourneyMove = moveRepository.findById(noJourneyMove.id).get()
        assertThat(retrievedNoJourneyMove.reference).isEqualTo("UKW4591N")
    }
}

