package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportReaderParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate
import java.time.LocalDateTime

internal class ImportServiceTest {

  private val timeSourceWithFixedDate: TimeSource = TimeSource { LocalDateTime.of(2021, 2, 18, 12, 0, 0) }
  private val priceImporter: PriceImporter = mock()
  private val reportImporter: ReportImporter = mock()
  private val movePersister: MovePersister = mock { on { persist(any()) } doReturn 1 }
  private val personPersister: PersonPersister = mock()
  private val move: Move = mock()
  private val auditService: AuditService = mock()
  private val monitoringService: MonitoringService = mock()
  private val importService: ImportService =
    ImportService(
      timeSourceWithFixedDate,
      priceImporter,
      reportImporter,
      movePersister,
      personPersister,
      auditService,
      monitoringService
    )

  @Test
  internal fun `price importer interactions for Serco`() {
    importService.importPrices(Supplier.SERCO, 2020)

    verify(priceImporter).import(Supplier.SERCO, 2020)
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `price importer interactions for GEOAmey`() {
    importService.importPrices(Supplier.GEOAMEY, 2020)

    verify(priceImporter).import(Supplier.GEOAMEY, 2020)
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `expected report importer interactions`() {
    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importPeople(any(), any())
    verify(reportImporter).importProfiles(any(), any())
  }

  @Test
  fun `expected audit interactions for moves, people and profiles `() {
    val service = ImportService(
      timeSourceWithFixedDate,
      priceImporter,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(),
      auditService,
      monitoringService
    )

    service.importReportsOn(timeSourceWithFixedDate.date())

    verify(auditService).create(AuditableEvent.importReportEvent("moves", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("people", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("profiles", timeSourceWithFixedDate.date(), 1, 1))
    verifyNoInteractions(monitoringService)
  }

  @Test
  fun `expected monitoring interactions when failure to persist moves`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf(move))
    whenever(movePersister.persist(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(monitoringService).capture("moves: persisted 0 out of 1 for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  fun `expected monitoring interactions when failure to persist people`() {
    val service = ImportService(
      timeSourceWithFixedDate,
      priceImporter,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(successful = false),
      auditService,
      monitoringService
    )

    service.importPeopleProfiles(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("people: persisted 0 and 1 errors for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when failure to persist profiles`() {
    val service = ImportService(
      timeSourceWithFixedDate,
      priceImporter,
      FakeReportImporter(),
      movePersister,
      FakePersonPersister(successful = false),
      auditService,
      monitoringService
    )

    service.importPeopleProfiles(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("profiles: persisted 0 and 1 errors for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  internal fun `expected monitoring interactions when no moves to persist`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(emptyList())

    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(monitoringService).capture("There were no moves to persist for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  fun `expected monitoring interactions when no people to persist`() {
    importService.importReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("There were no people to persist for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `expected monitoring interactions when no profiles to persist`() {
    importService.importReportsOn(timeSourceWithFixedDate.yesterday())

    verify(monitoringService).capture("There were no profiles to persist for reporting feed date ${timeSourceWithFixedDate.yesterday()}.")
  }

  @Test
  fun `given an import date range one day ensure only one call is made`() {
    importService.importReportsOn(DateRange(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date()))

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importPeople(any(), any())
    verify(reportImporter).importProfiles(any(), any())
  }

  @Test
  fun `given an import date range of two days ensure multiple calls are made`() {
    importService.importReportsOn(DateRange(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date().plusDays(1)))

    verify(reportImporter, times(1)).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter, times(1)).importMovesJourneysEventsOn(timeSourceWithFixedDate.date().plusDays(1))
    verify(reportImporter, times(2)).importPeople(any(), any())
    verify(reportImporter, times(2)).importProfiles(any(), any())
  }

  @Test
  fun `given an import date of yesterday ensure only one call is made when importing moves`() {
    importService.importMoves(timeSourceWithFixedDate.yesterday())

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.yesterday())
  }

  @Test
  fun `given an import date of yesterday ensure only one call is made when importing people and profiles`() {
    importService.importPeopleProfiles(timeSourceWithFixedDate.yesterday())

    verify(reportImporter).importPeople(any(), any())
    verify(reportImporter).importProfiles(any(), any())
  }

  @Test
  fun `given an import date of two days ago ensure two calls are made when importing people and profiles`() {
    importService.importPeopleProfiles(timeSourceWithFixedDate.yesterday().minusDays(1))

    verify(reportImporter, times(2)).importPeople(any(), any())
    verify(reportImporter, times(2)).importProfiles(any(), any())
  }

  @Test
  fun `given an import date of current or future date an exception is thrown when importing people and profiles`() {
    assertThatThrownBy {
      importService.importPeopleProfiles(timeSourceWithFixedDate.date())
    }.isInstanceOf(RuntimeException::class.java)

    assertThatThrownBy {
      importService.importPeopleProfiles(timeSourceWithFixedDate.date().plusDays(1))
    }.isInstanceOf(RuntimeException::class.java)
  }

  private class FakeReportImporter : ReportImporter(mock(), mock(), FakeReportReaderParser()) {
    override fun importMovesJourneysEventsOn(date: LocalDate) = listOf<Move>(mock())
  }

  private class FakeReportReaderParser : ReportReaderParser {
    override fun <T> forEach(reportName: String, parser: (String) -> T?, consumer: (T) -> Unit) {
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
