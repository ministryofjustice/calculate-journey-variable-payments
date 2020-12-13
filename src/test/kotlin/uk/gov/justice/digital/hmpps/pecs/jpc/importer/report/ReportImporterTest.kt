package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")
/**
 * This uses the test move data files in test/resources/move
 * The ReportingProvider uses the local filesystem as defined in TestConfig
 */
internal class ReportImporterTest(@Autowired provider: ReportingProvider, @Autowired timeSource: TimeSource) {

    val importer: ReportImporter = ReportImporter(provider, timeSource)

    val from = LocalDate.of(2020, 9, 1)
    val to = LocalDate.of(2020, 9, 6)

    private lateinit var moves: Collection<Report>

    @BeforeEach
    fun beforeEach() {
     moves = importer.importMovesJourneysEvents(from, to)
    }

    @Test
    fun `For the 3rd of the month, starting on the 2nd should return 2 file names`() {
        val from =  LocalDate.of(2020, 8, 30)
        val to =  LocalDate.of(2020, 8, 31)
        val fileNames = ReportImporter.fileNamesForDate("moves", from, to)
        Assertions.assertEquals(listOf("2020/08/30/2020-08-30-moves.jsonl", "2020/08/31/2020-08-31-moves.jsonl"), fileNames)
    }

//    @Test
//    fun `Person populated correctly`(){
//        // This should pick up the latest person file
//        val person = moves.toList()[0].person
//        assertThat(person?.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
//        assertThat(person?.prisonNumber).isEqualTo("PRISON1")
//
//    }

    @Test
    fun `Get files for date should ignore missing days`() {
        val content = importer.importMovesJourneysEvents(
                LocalDate.of(2020, 9, 1),
                LocalDate.of(2020, 9, 3))

        // This should only pick up the completed and cancelled moves
        Assertions.assertEquals(setOf("M1", "M2", "M20",  "M3", "M30"), content.map { it.move.moveId }.toSet())
    }

    @Test
    fun `Standard moves should only include completed moves with one billable, completed journey`() {
        val standardReports = ReportFilterer.standardMoveReports(moves)
        Assertions.assertEquals(setOf("M2", "M3"), standardReports.map { it.move.moveId }.toSet())
    }

    @Test
    fun `Redirect moves should only include completed moves with two billable journeys`() {
        val redirectionReports = ReportFilterer.redirectionReports(moves)
        Assertions.assertEquals(setOf("M20"), redirectionReports.map { it.move.moveId }.toSet())
    }

    @Test
    fun `Long haul moves should only include completed moves with two billable journeys`() {
        val longHaulReports = ReportFilterer.longHaulReports(moves)
        Assertions.assertEquals(setOf("M30"), longHaulReports.map { it.move.moveId }.toSet())
    }

    @Test
    fun `Cancelled, billable moves should only include moves cancelled before 3pm the day before the move`() {
        val cancelledBillableReports = ReportFilterer.cancelledBillableMoves(moves)
        Assertions.assertEquals(setOf("M61"), cancelledBillableReports.map { it.move.moveId }.toSet())
    }
}
