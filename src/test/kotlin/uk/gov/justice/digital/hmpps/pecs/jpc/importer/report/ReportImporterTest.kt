package uk.gov.justice.digital.hmpps.pecs.jpc.importer.move

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
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")
/**
 * This uses the test move data files in test/resources/move
 * The ReportingProvider uses the local filesystem as defined in TestConfig
 */
internal class ReportingImporterTest(@Autowired provider: ReportingProvider, @Autowired timeSource: TimeSource) {

    val importer: ReportImporter = ReportImporter(provider, timeSource)

    val from = LocalDate.of(2020, 9, 1)
    val to = LocalDate.of(2020, 9, 6)

    private lateinit var moves: Collection<Move>

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

    @Test
    fun `Get files for date should ignore missing days`() {
        val content = importer.importMovesJourneysEvents(
                LocalDate.of(2020, 9, 1),
                LocalDate.of(2020, 9, 3))

        // This should only pick up the completed and cancelled moves
        Assertions.assertEquals(setOf("M1", "M2", "M20",  "M3", "M30"), content.map { it.moveId }.toSet())
    }

    @Test
    fun `Standard moves should only include completed moves with one billable, completed journey`() {
        val standardMoves = movesFilteredBy(MoveFilterer::isStandardMove)
        Assertions.assertEquals(setOf("M2", "M3"), standardMoves.map { it.moveId }.toSet())
    }

    @Test
    fun `Redirect moves should only include completed moves with two billable journeys`() {
        val redirectionMoves = movesFilteredBy(MoveFilterer::isRedirectionMove)
        Assertions.assertEquals(setOf("M20"), redirectionMoves.map { it.moveId }.toSet())
    }

    @Test
    fun `Long haul moves should only include completed moves with two billable journeys`() {
        val longHaulMoves = movesFilteredBy(MoveFilterer::isLongHaulMove)
        Assertions.assertEquals(setOf("M30"), longHaulMoves.map { it.moveId }.toSet())
    }

    @Test
    fun `Cancelled, billable moves should only include moves cancelled before 3pm the day before the move`() {
        val cancelledBillableMoves = movesFilteredBy(MoveFilterer::isCancelledBillableMove)
        Assertions.assertEquals(setOf("M61"), cancelledBillableMoves.map { it.moveId }.toSet())
    }

    private fun movesFilteredBy(filterer: (m: Move) -> Boolean) = moves.filter(filterer)
}
