package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
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
    importService.importPrices(Supplier.SERCO)

    verify(priceImporter).import(Supplier.SERCO)
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `price importer interactions for GEOAmey`() {
    importService.importPrices(Supplier.GEOAMEY)

    verify(priceImporter).import(Supplier.GEOAMEY)
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `report importer interactions`() {
    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(reportImporter).importMovesJourneysEventsOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importPeopleOn(timeSourceWithFixedDate.date())
    verify(reportImporter).importProfilesOn(timeSourceWithFixedDate.date())
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `auditing interactions`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf(move))
    whenever(reportImporter.importPeopleOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(person))
    whenever(reportImporter.importProfilesOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(profile))

    importService.importReportsOn(timeSourceWithFixedDate.date())

    verify(auditService).create(AuditableEvent.importReportEvent("moves", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("people", timeSourceWithFixedDate.date(), 1, 1))
    verify(auditService).create(AuditableEvent.importReportEvent("profiles", timeSourceWithFixedDate.date(), 1, 1))
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring interactions when failure to persist moves`() {
    whenever(reportImporter.importMovesJourneysEventsOn(timeSourceWithFixedDate.date())).thenReturn(listOf(move))
    whenever(movePersister.persist(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("moves: persisted 0 out of 1 for reporting date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `monitoring interactions when failure to persist people`() {
    whenever(reportImporter.importPeopleOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(person))
    whenever(personPersister.persistPeople(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("people: persisted 0 out of 1 for reporting date ${timeSourceWithFixedDate.date()}.")
  }

  @Test
  internal fun `monitoring interactions when failure to persist profiles`() {
    whenever(reportImporter.importProfilesOn(timeSourceWithFixedDate.date())).thenReturn(sequenceOf(profile))
    whenever(personPersister.persistProfiles(any())).thenReturn(0)

    importService.importReportsOn(timeSourceWithFixedDate.date())
    verify(monitoringService).capture("profiles: persisted 0 out of 1 for reporting date ${timeSourceWithFixedDate.date()}.")
  }
}
