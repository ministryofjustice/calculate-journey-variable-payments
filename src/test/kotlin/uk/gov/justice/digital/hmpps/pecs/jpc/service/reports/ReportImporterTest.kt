package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * This uses the test move data files in test/resources/move
 * The ReportingProvider uses the local filesystem as defined in TestConfig
 */
@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")
internal class ReportImporterTest(
  @Autowired private val provider: ReportingProvider,
  @Autowired val reportReaderParser: StandardStreamingReportParser,
) {

  @MockBean
  private lateinit var monitoringService: MonitoringService

  private lateinit var importer: ReportImporter

  private val from: LocalDate = LocalDate.of(2020, 9, 1)
  private val to: LocalDate = LocalDate.of(2020, 9, 6)

  private var moves: Collection<Move> = mutableListOf()

  @BeforeEach
  fun beforeEach() {
    importer = ReportImporter(provider, monitoringService, reportReaderParser)

    for (i in 0..ChronoUnit.DAYS.between(from, to)) {
      moves += importer.importMovesJourneysEventsOn(from.plusDays(i))
    }
  }

  @Test
  fun `For the 3rd of the month, starting on the 2nd should return 2 file names`() {
    val from = LocalDate.of(2020, 8, 30)
    val to = LocalDate.of(2020, 8, 31)
    val fileNames = ReportImporter.fileNamesForDate("moves", from, to)
    Assertions.assertEquals(listOf("2020/08/30/2020-08-30-moves.jsonl", "2020/08/31/2020-08-31-moves.jsonl"), fileNames)
  }

  @Test
  fun `Get files for date should ignore missing days`() {
    var content = importer.importMovesJourneysEventsOn(LocalDate.of(2020, 9, 1))
    content += importer.importMovesJourneysEventsOn(LocalDate.of(2020, 9, 2))
    content += importer.importMovesJourneysEventsOn(LocalDate.of(2020, 9, 3))

    // This should only pick up the completed and cancelled moves
    Assertions.assertEquals(setOf("M1", "M2", "M20", "M3", "M30"), content.map { it.moveId }.toSet())
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

  @Test
  fun `Monitoring service captures file download errors for moves, journeys and events`() {
    val failingProvider: ReportingProvider = mock()

    whenever(failingProvider.get(any())).thenThrow(RuntimeException("error"))

    ReportImporter(
      failingProvider,
      monitoringService,
      reportReaderParser,
    ).importMovesJourneysEventsOn(LocalDate.of(2020, 9, 1))

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file 2020/09/01/2020-09-01-moves.jsonl. Exception: error")
    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file 2020/09/01/2020-09-01-journeys.jsonl. Exception: error")
    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file 2020/09/01/2020-09-01-events.jsonl. Exception: error")
  }

  @Test
  fun `Monitoring service captures file download errors for profiles`() {
    ReportImporter(provider, monitoringService, FailingStreamingReportParser("profile error")).importProfiles(
      LocalDate.of(
        2020,
        9,
        2,
      ),
    ) { }

    verify(monitoringService).capture("Error processing profiles file 2020/09/02/2020-09-02-profiles.jsonl, exception: profile error")
  }

  @Test
  fun `Monitoring service captures file download errors for people`() {
    ReportImporter(provider, monitoringService, FailingStreamingReportParser("people error")).importPeople(
      LocalDate.of(
        2020,
        10,
        3,
      ),
    ) { }

    verify(monitoringService).capture("Error processing people file 2020/10/03/2020-10-03-people.jsonl, exception: people error")
  }

  @Test
  fun `report filenames for a given date`() {
    assertThat(ReportImporter.reportFilenamesFor(LocalDate.of(2022, 6, 15))).containsExactlyInAnyOrder(
      "2022/06/15/2022-06-15-moves.jsonl",
      "2022/06/15/2022-06-15-journeys.jsonl",
      "2022/06/15/2022-06-15-events.jsonl",
      "2022/06/15/2022-06-15-profiles.jsonl",
      "2022/06/15/2022-06-15-people.jsonl",
    )

    assertThat(ReportImporter.reportFilenamesFor(LocalDate.of(2022, 6, 14))).containsExactlyInAnyOrder(
      "2022/06/14/2022-06-14-moves.jsonl",
      "2022/06/14/2022-06-14-journeys.jsonl",
      "2022/06/14/2022-06-14-events.jsonl",
      "2022/06/14/2022-06-14-profiles.jsonl",
      "2022/06/14/2022-06-14-people.jsonl",
    )
  }

  private class FailingStreamingReportParser(private val errorMessage: String) : StreamingReportParser {
    override fun <T> forEach(reportName: String, parse: (String) -> T?, consumer: (T) -> Unit): Unit = throw RuntimeException(errorMessage)
  }

  private fun movesFilteredBy(filterer: (m: Move) -> Boolean) = moves.filter(filterer)
}
