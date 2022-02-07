package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
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
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDateTime

internal class ImportServiceTest {

  private val timeSourceWithFixedDate: TimeSource = TimeSource { LocalDateTime.of(2021, 2, 18, 12, 0, 0) }
  private val priceImporter: PriceImporter = mock()
  private val reportImporter: ReportImporter = mock()
  private val movePersister: MovePersister = mock { on { persist(any()) } doReturn 1 }
  private val personPersister: PersonPersister = mock {
    on { persistPeople(any()) } doReturn 1
    on { persistProfiles(any()) } doReturn 1
  }
  private val move: Move = mock()
  private val person: Person = mock()
  private val profile: Profile = mock()
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
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.date())
  }

  @Test
  internal fun `expected audit interactions for moves, people and profiles `() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf(move))
    whenever(reportImporter.importPeopleOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(person))
    whenever(reportImporter.importProfilesOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(profile))

    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(auditService).create(AuditableEvent.importReportEvent("moves", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("people", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("profiles", timeSourceWithFixedDate.date(), 1, 1))
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `expected monitoring interactions when failure to persist moves`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf(move))
    whenever(movePersister.persist(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("moves: persisted 0 out of 1 for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `expected monitoring interactions when failure to persist people`() {
    whenever(reportImporter.importPeopleOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(person))
    whenever(personPersister.persistPeople(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("people: persisted 0 out of 1 for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `expected monitoring interactions when failure to persist profiles`() {
    whenever(reportImporter.importProfilesOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(profile))
    whenever(personPersister.persistProfiles(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("profiles: persisted 0 out of 1 for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `expected monitoring interactions when no moves to persist`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf())

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("There were no moves to persist for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `expected monitoring interactions when no people to persist`() {
    whenever(reportImporter.importPeopleOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf())

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("There were no people to persist for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `expected monitoring interactions when no profiles to persist`() {
    whenever(reportImporter.importProfilesOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf())

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("There were no profiles to persist for reporting feed date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  fun `given an import date range one day ensure only one call is made`() {
    importService.importReportsOn(DateRange(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date()))

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.date())
  }

  @Test
  fun `given an import date range of two days ensure multiple calls are made`() {
    importService.importReportsOn(DateRange(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date().plusDays(1)))

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date().plusDays(1))
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.date().plusDays(1))
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.date().plusDays(1))
  }

  @Test
  fun `given an import date of yesterday ensure only one call is made when importing moves`() {
    importService.importMoves(timeSourceWithFixedDate.yesterday())

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.yesterday())
  }

  @Test
  fun `given an import date of yesterday ensure only one call is made when importing people and profiles`() {
    importService.importPeopleProfiles(timeSourceWithFixedDate.yesterday())

    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.yesterday())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.yesterday())
  }

  @Test
  fun `given an import date of two days ago ensure two calls are made when importing people and profiles`() {
    importService.importPeopleProfiles(timeSourceWithFixedDate.yesterday().minusDays(1))

    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.yesterday().minusDays(1))
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.yesterday())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.yesterday().minusDays(1))
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.yesterday())
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
}
