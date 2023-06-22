package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportLookup
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.LocalDate
import java.time.LocalDateTime

internal class ImportReportServiceTest {

  private val timeSourceWithFixedDate: TimeSource = TimeSource { LocalDateTime.of(2021, 2, 18, 12, 0, 0) }
  private val reportImporter: ReportImporter = mock()
  private val movePersister: MovePersister = mock { on { persist(any()) } doReturn 1 }
  private val personPersister: PersonPersister = mock()
  private val move: Move = mock()
  private val auditService: AuditService = mock()
  private val monitoringService: MonitoringService = mock()
  private val reportLookup: ReportLookup = ReportLookup { true }
  private val importReportsService: ImportReportsService =
    ImportReportsService(
      timeSourceWithFixedDate,
      reportImporter,
      movePersister,
      personPersister,
      auditService,
      monitoringService,
      reportLookup,
    )

  @Test
  internal fun `expected report importer interactions`() {
    importReportsService.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.yesterday())
    verify(reportImporter).importPeople(eq(timeSourceWithFixedDate.yesterday()), any())
    verify(reportImporter).importProfiles(eq(timeSourceWithFixedDate.yesterday()), any())
  }

  @Test
  fun `expected audit interactions for moves, people and profiles `() {
    val service = ImportReportsService(
      timeSourceWithFixedDate,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(),
      auditService,
      monitoringService,
      reportLookup,
    )

    service.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(auditService).create(
      AuditableEvent.importReportsEvent(
        timeSourceWithFixedDate.yesterday(),
        1,
        1,
        1,
        1,
        1,
        1,
      ),
    )
    verifyNoInteractions(monitoringService)
  }

  @Test
  fun `expected monitoring interactions when failure to persist moves`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.yesterday())).thenReturn(listOf(move))
    whenever(movePersister.persist(any())).thenReturn(0)

    importReportsService.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("moves: persisted 0 out of 1 for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when failure to persist people`() {
    val service = ImportReportsService(
      timeSourceWithFixedDate,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(successful = false),
      auditService,
      monitoringService,
      reportLookup,
    )

    service.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("people: persisted 0 and 1 errors for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when failure to persist profiles`() {
    val service = ImportReportsService(
      timeSourceWithFixedDate,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(successful = false),
      auditService,
      monitoringService,
      reportLookup,
    )

    service.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("profiles: persisted 0 and 1 errors for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  internal fun `expected monitoring interactions when no moves to persist`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(emptyList())

    importReportsService.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("There were no moves to persist for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when no people to persist`() {
    importReportsService.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("There were no people to persist for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when no profiles to persist`() {
    importReportsService.importAllReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("There were no profiles to persist for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `fails if import date is not in the past`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(timeSourceWithFixedDate.date()) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Import date must be in the past.")
  }

  @Test
  fun `expected monitoring interactions when report files missing`() {
    val service = ImportReportsService(
      timeSourceWithFixedDate,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(successful = false),
      auditService,
      monitoringService,
      ReportLookup { false },
    )

    assertThatThrownBy { service.importAllReportsOn(timeSourceWithFixedDate.yesterday()) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("The service is missing data which may affect pricing due to missing file(s): 2021/02/17/2021-02-17-moves.jsonl, 2021/02/17/2021-02-17-events.jsonl, 2021/02/17/2021-02-17-journeys.jsonl, 2021/02/17/2021-02-17-profiles.jsonl, 2021/02/17/2021-02-17-people.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2021/02/17/2021-02-17-moves.jsonl, 2021/02/17/2021-02-17-events.jsonl, 2021/02/17/2021-02-17-journeys.jsonl, 2021/02/17/2021-02-17-profiles.jsonl, 2021/02/17/2021-02-17-people.jsonl")
  }

  private class FakeReportImporter : ReportImporter(mock(), mock(), FakeStreamingReportParser()) {
    override fun importMovesJourneysEventsOn(date: LocalDate) = listOf<Move>(mock())
  }

  private class FakeStreamingReportParser : StreamingReportParser {
    override fun <T> forEach(reportName: String, parse: (String) -> T?, consumer: (T) -> Unit) {
      @Suppress("UNCHECKED_CAST")
      if (reportName.contains("profile")) consumer(mock<Profile>() as T) else consumer(mock<Person>() as T)
    }
  }

  private class FakePersonPersister(private val successful: Boolean = true) : PersonPersister(mock(), mock()) {
    override fun persistProfile(profile: Profile, success: () -> Unit, failure: (Throwable) -> Unit) {
      if (successful) success() else failure(RuntimeException("error"))
    }

    override fun persistPerson(person: Person, success: () -> Unit, failure: (Throwable) -> Unit) {
      if (successful) success() else failure(RuntimeException("error"))
    }
  }
}
