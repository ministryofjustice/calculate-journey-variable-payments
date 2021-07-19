package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.Duration
import java.time.LocalDate

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

  private val logger = LoggerFactory.getLogger(javaClass)

  @Transactional
  fun importPrices(supplier: Supplier, year: Int) = import { priceImporter.import(supplier, year) }

  // The transaction boundary for this method is being set in the underlying persistence classes on purpose.
  fun importReportsOn(date: LocalDate) {
    importMovesJourneysEventsOn(date)
    importPeopleProfilesOn(date)
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

  private fun importPeopleProfilesOn(date: LocalDate) {
    logger.info("Importing people for date: $date.")

    import { reportImporter.importPeopleOn(date) }?.let {
      val people = it.toList()
      personPersister.persistPeople(people).let { persisted ->
        auditService.create(AuditableEvent.importReportEvent("people", date, people.size, persisted))

        raiseMonitoringAlertIf(
          people.isNotEmpty() && people.size > persisted,
          "people: persisted $persisted out of ${people.size} for reporting feed date $date."
        )

        raiseMonitoringAlertIf(people.isEmpty(), "There were no people to persist for reporting feed date $date.")
      }
    }

    logger.info("Importing profiles for date: $date.")

    import { reportImporter.importProfilesOn(date) }?.let {
      val profiles = it.toList()
      personPersister.persistProfiles(profiles).let { persisted ->
        auditService.create(AuditableEvent.importReportEvent("profiles", date, profiles.size, persisted))

        raiseMonitoringAlertIf(
          profiles.isNotEmpty() && profiles.size > persisted,
          "profiles: persisted $persisted out of ${profiles.size} for reporting feed date $date."
        )

        raiseMonitoringAlertIf(profiles.isEmpty(), "There were no profiles to persist for reporting feed date $date.")
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
