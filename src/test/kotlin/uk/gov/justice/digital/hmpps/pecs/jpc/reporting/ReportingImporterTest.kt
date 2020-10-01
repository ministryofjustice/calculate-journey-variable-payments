package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")

/**
 * This uses the test reporting data files in test/resources/reporting
 * The ReportingProvider uses the local filesystem as defined in TestConfig
 */
internal class ReportingImporterTest {

    @Autowired
    lateinit var importer: ReportingImporter

    @Autowired
    lateinit var provider: ReportingProvider

    @Test
    fun `For the 3rd of the month, starting on the 2nd should return 2 file names`() {
        val from =  LocalDate.of(2020, 8, 30)
        val to =  LocalDate.of(2020, 8, 31)
        val fileNames = ReportingImporter.fileNamesForDate("moves", from, to)
        Assertions.assertEquals(listOf("2020/08/30/2020-08-30-moves.jsonl", "2020/08/31/2020-08-31-moves.jsonl"), fileNames)
    }


    @Test
    fun `Get files for date should ignore missing days`() {
        val content = importer.import(
                LocalDate.of(2020, 9, 1),
                LocalDate.of(2020, 9, 3))

        // This should only pick up the completed moves
        Assertions.assertEquals(setOf("M1", "M3",  "M5"), content.map { it.move.id }.toSet())
    }

    @Test
    fun `Standard moves should only include completed moves with one billable, completed journey`() {
        val movesFrom = LocalDate.of(2020, 9, 2)
        val movesTo = LocalDate.of(2020, 9, 2)
        val allMoves = importer.import(movesFrom, movesTo)
        val standardMoves = MoveFilterer.standardMoves(MoveFiltererParams(Supplier.GEOAMEY, movesFrom, movesTo), allMoves)

        // M1 should be the only standard move
        Assertions.assertEquals(setOf("M1"), standardMoves.map { it.move.id }.toSet())
    }

}

