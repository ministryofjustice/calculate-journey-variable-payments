package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
internal class ReportModelRepositoryTest {

    @Autowired
    lateinit var reportModelRepository: ReportModelRepository

    @Autowired
    lateinit var reportJourneyModelRepository: ReportJourneyModelRepository


    @Autowired
    lateinit var entityManager: TestEntityManager

    val moveDate = LocalDate.of(2020, 9, 10)


    @Test
    fun `save report model`() {

        val report1 = ReportModel(supplier = Supplier.SERCO, movePriceType = MovePriceType.STANDARD, moveDate = moveDate, report = "blah")
        val journey1 = ReportJourneyModel(report = report1, fromNomisAgencyId = "ABV", toNomisAgencyId = "DEF", billable = true)
        val journey2 = ReportJourneyModel(report = report1, fromNomisAgencyId = "GHI", toNomisAgencyId = "JKL", billable = false)

        report1.addJourneys(journey1, journey2)

        val persistedReport = reportModelRepository.save(report1)

        entityManager.flush()
        entityManager.clear()

        println("XXXXXXXXX")

        val retrievedReport = reportModelRepository.findById(persistedReport.id).get()

        assertThat(retrievedReport).isEqualTo(report1)

        assertThat(retrievedReport.journeys).containsExactlyInAnyOrder(journey1, journey2)
    }

    @Test
    fun `find all report journeys`() {
        val report1 = ReportModel(supplier = Supplier.SERCO, movePriceType = MovePriceType.STANDARD, moveDate = moveDate, report = "blah")
        val journey1 = ReportJourneyModel(report = report1, fromNomisAgencyId = "ABD", toNomisAgencyId = "DEF", billable = true)
        report1.addJourneys(journey1)

        val report2 = ReportModel(supplier = Supplier.GEOAMEY, movePriceType = MovePriceType.STANDARD, moveDate = moveDate, report = "blah")
        val journey2 = ReportJourneyModel(report = report2, fromNomisAgencyId = "HIJ", toNomisAgencyId = "KLM", billable = true)
        report2.addJourneys(journey2)

        reportModelRepository.saveAll(listOf(report1, report2))

        entityManager.flush()
        entityManager.clear()

        println("XXXXXXXXX")

        val journeys = reportJourneyModelRepository.findAll()

        println(journeys)

    }
}
