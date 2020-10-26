package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
internal class MoveModelRepositoryTest {

    @Autowired
    lateinit var moveModelRepository: MoveModelRepository

    @Autowired
    lateinit var journeyModelRepository: JourneyModelRepository

    @Autowired
    lateinit var locationRepository: LocationRepository


    @Autowired
    lateinit var entityManager: TestEntityManager

    val moveDate = LocalDate.of(2020, 9, 10)

    val report1 = MoveModel(
            moveId = UUID.randomUUID().toString(),
            supplier = Supplier.SERCO,
            movePriceType = MovePriceType.STANDARD,
            status = MoveStatus.COMPLETED,
            reference = "REF1",
            moveDate = moveDate,
            fromNomisAgencyId = "YUI",
            toNomisAgencyId = "GNI",
            pickUp = moveDate.atStartOfDay(),
            dropOffOrCancelled = moveDate.atStartOfDay().plusHours(10),
            notes = "some notes",
            prisonNumber = "PR101",
            vehicleRegistration = "blah")

    val journey1 = JourneyModel(
            journeyId = UUID.randomUUID().toString(),
            state = JourneyState.COMPLETED,
            move = report1,
            fromNomisAgencyId = "ABV",
            toNomisAgencyId = "DEF",
            billable = true,
            notes = "some notes"
    )
    val journey2 = JourneyModel(
            journeyId = UUID.randomUUID().toString(),
            state = JourneyState.COMPLETED,
            move = report1,
            fromNomisAgencyId = "GHI",
            toNomisAgencyId = "JKL",
            billable = false,
            notes = "some notes"
    )


    @Test
    fun `save report model`() {

        val from = locationRepository.save(fromPrisonLocationFactory())
        val to = locationRepository.save(toCourtLocationFactory())

        report1.addJourneys(journey1.copy(fromLocation = from, toLocation = to), journey2)
        val persistedReport = moveModelRepository.save(report1.copy(fromLocation = from, toLocation = to))

        entityManager.flush()
        entityManager.clear()

        println("XXXXXXXXX")

        val retrievedReport = moveModelRepository.findById(persistedReport.moveId).get()

        assertThat(retrievedReport).isEqualTo(report1)

        println("move: " + retrievedReport)
//        println("move from: " + retrievedReport.fromLocation)
//        println("move to: " + retrievedReport.toLocation)
//        println("journey from: " + retrievedReport.journeys[0].fromLocation)
//        println("journey to: " + retrievedReport.journeys[0].toLocation)

//        assertThat(retrievedReport.journeys).containsExactlyInAnyOrder(journey1, journey2)
    }

    @Test
    fun `find all report journeys`() {

        val report2 = MoveModel(
                moveId = UUID.randomUUID().toString(),
                supplier = Supplier.GEOAMEY,
                movePriceType = MovePriceType.STANDARD,
                status = MoveStatus.COMPLETED,
                reference = "REF2",
                moveDate = moveDate,
                fromNomisAgencyId = "ABC",
                toNomisAgencyId = "EFG",
                pickUp = moveDate.atStartOfDay(),
                dropOffOrCancelled = moveDate.atStartOfDay().plusHours(10),
                notes = "some notes",
                prisonNumber = "PR102",
                vehicleRegistration = "blah2")

        val journey3 = JourneyModel(
                journeyId = UUID.randomUUID().toString(),
                state = JourneyState.COMPLETED,
                move = report2,
                fromNomisAgencyId = "GGG",
                toNomisAgencyId = "HHH",
                billable = true,
                notes = "some notes"
        )

        report1.addJourneys(journey1, journey2)
        report2.addJourneys(journey3)


        moveModelRepository.saveAll(listOf(report1, report2))

        entityManager.flush()
        entityManager.clear()

        println("XXXXXXXXX")

        val journeys = journeyModelRepository.findAll()

        println(journeys)

    }
}
