package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val logger = loggerFor<ImportService>()

@Service
class ImportService(
  private val timeSource: TimeSource,
  private val priceImporter: PriceImporter,
  private val reportImporter: ReportImporter,
  private val movePersister: MovePersister,
  private val personPersister: PersonPersister,
  private val auditService: AuditService,
  private val monitoringService: MonitoringService
) {

  @Transactional
  fun importPrices(supplier: Supplier, year: Int) = import { priceImporter.import(supplier, year) }

  fun importReportsOn(range: ClosedRange<LocalDate>) {
    for (i in 0..ChronoUnit.DAYS.between(range.start, range.endInclusive)) {
      importReportsOn(range.start.plusDays(i))
    }
  }

  // The transaction boundary for this method is being set in the underlying persistence classes on purpose.
  fun importReportsOn(date: LocalDate) {
    importMovesJourneysEventsOn(date)
    importPeopleProfilesOn(date)
  }

  fun importMoves(date: LocalDate) {
    importMovesJourneysEventsOn(date)
  }

  private fun importMovesJourneysEventsOn(date: LocalDate) {
    logger.info("Importing moves, journeys and events for date: $date.")

    import { reportImporter.importMovesJourneysEventsOn(date) }?.let {
      val moves = it.toList()
      movePersister.persist(moves).let { persisted ->
        auditService.create(AuditableEvent.importReportEvent("moves", date, moves.size, persisted))

        raiseMonitoringAlertIf(
          moves.isNotEmpty() && moves.size > persisted,
          "moves: persisted $persisted out of ${moves.size} for reporting feed date $date."
        )

        raiseMonitoringAlertIf(moves.isEmpty(), "There were no moves to persist for reporting feed date $date.")
      }
    }
  }

  /**
   * This will import all people and profiles starting from the date supplied upto the current date minus one day. This
   * is needed to ensure the data is as up-to-date as possible.
   */
  fun importPeopleProfiles(from: LocalDate) {
    DateRange(from, timeSource.yesterday()).run {
      for (i in 0..ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
        importPeopleProfilesOn(this.start.plusDays(i))
      }
    }
  }

  private fun importPeopleProfilesOn(date: LocalDate) {
    logger.info("Importing people for date: $date.")

    import { reportImporter.importPeopleOn(date) }?.let {
      personPersister.persistPeople(it).let { result ->
        auditService.create(AuditableEvent.importReportEvent("people", date, result.processed(), result.persisted))

        raiseMonitoringAlertIf(
          result.errors > 0,
          "people: persisted ${result.persisted} and ${result.errors} errors for reporting feed date $date."
        )

        raiseMonitoringAlertIf(result.processed() == 0, "There were no people to persist for reporting feed date $date.")
      }
    }

    logger.info("Importing profiles for date: $date.")

    import { reportImporter.importProfilesOn(date) }?.let {
      personPersister.persistProfiles(it).let { result ->
        auditService.create(AuditableEvent.importReportEvent("profiles", date, result.processed(), result.persisted))

        raiseMonitoringAlertIf(
          result.errors > 0,
          "profiles: persisted ${result.persisted} and ${result.errors} errors for reporting feed date $date."
        )

        raiseMonitoringAlertIf(result.processed() == 0, "There were no profiles to persist for reporting feed date $date.")
      }
    }
  }

  private fun raiseMonitoringAlertIf(isTrue: Boolean, alertMessage: String) {
    if (isTrue) monitoringService.capture(alertMessage)
  }

  private fun <T> import(import: () -> T): T? {
    logger.info("Attempting import of ${import.javaClass}")

    val start = timeSource.dateTime()

    try {
      return import()
    } finally {
      val end = timeSource.dateTime()
      logger.info("Import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
    }
  }
}
