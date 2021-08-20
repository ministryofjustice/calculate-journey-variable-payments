package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")
/**
 * This uses the test move data files in test/resources/move
 * The ReportingProvider uses the local filesystem as defined in TestConfig
 */
internal class ReportImporterTest(@Autowired private val provider: ReportingProvider) {

  @MockBean
  private lateinit var monitoringService: MonitoringService

  private lateinit var importer: ReportImporter

  private val from: LocalDate = LocalDate.of(2020, 9, 1)
  private val to: LocalDate = LocalDate.of(2020, 9, 6)

  private var moves: Collection<Move> = mutableListOf()

  @BeforeEach
  fun beforeEach() {
    importer = ReportImporter(provider, monitoringService)

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

    ReportImporter(failingProvider, monitoringService).importMovesJourneysEventsOn(LocalDate.of(2020, 9, 1))

    verify(monitoringService).capture("Error attempting to get moves file 2020/09/01/2020-09-01-moves.jsonl, exception: error")
    verify(monitoringService).capture("Error attempting to get journeys file 2020/09/01/2020-09-01-journeys.jsonl, exception: error")
    verify(monitoringService).capture("Error attempting to get events file 2020/09/01/2020-09-01-events.jsonl, exception: error")
  }

  @Test
  fun `Monitoring service captures file download errors for profiles`() {
    val failingProvider: ReportingProvider = mock()

    whenever(failingProvider.get(any())).thenThrow(RuntimeException("error"))

    ReportImporter(failingProvider, monitoringService).importProfilesOn(LocalDate.of(2020, 9, 2))

    verify(monitoringService).capture("Error attempting to get profiles file 2020/09/02/2020-09-02-profiles.jsonl, exception: error")
  }

  @Test
  fun `Monitoring service captures file download errors for people`() {
    val failingProvider: ReportingProvider = mock()

    whenever(failingProvider.get(any())).thenThrow(RuntimeException("error"))

    ReportImporter(failingProvider, monitoringService).importPeopleOn(LocalDate.of(2020, 9, 3))

    verify(monitoringService).capture("Error attempting to get people file 2020/09/03/2020-09-03-people.jsonl, exception: error")
  }

  private fun movesFilteredBy(filterer: (m: Move) -> Boolean) = moves.filter(filterer)
}
