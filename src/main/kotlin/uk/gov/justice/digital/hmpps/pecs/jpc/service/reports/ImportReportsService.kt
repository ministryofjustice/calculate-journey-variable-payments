package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType.REPORTING_DATA_IMPORT
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service for importing move, journey, event, people and profile reports from AWS S3.
 */
private val logger = loggerFor<ImportReportsService>()

@Service
class ImportReportsService(
  private val timeSource: TimeSource,
  private val reportImporter: ReportImporter,
  private val movePersister: MovePersister,
  private val personPersister: PersonPersister,
  private val auditService: AuditService,
  private val monitoringService: MonitoringService
) {

  /**
   * Null implies there are no imports.
   */
  fun dateOfLastImport(): LocalDate? =
    auditService.findMostRecentEventByType(REPORTING_DATA_IMPORT)?.createdAt?.toLocalDate()

  fun importAllReportsOn(date: LocalDate) {
    // TODO check files exist before attempting to import, raise sentry alert if missing anything followed by a runtime exception
    importMovesJourneysEventsOn(date)
    importPeopleOn(date)
    importProfilesOn(date)
  }

  fun importMoveJourneyAndEventReportsOn(date: LocalDate) {
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
  fun importPeopleProfileReportsStartingFrom(from: LocalDate) {
    DateRange(from, timeSource.yesterday()).run {
      for (i in 0..ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
        importPeopleOn(this.start.plusDays(i))
        importProfilesOn(this.start.plusDays(i))
      }
    }
  }

  private fun importPeopleOn(date: LocalDate) {
    logger.info("Importing people for date: $date.")

    import {
      val (saved, errors) = AtomicInteger(0) to AtomicInteger(0)

      reportImporter.importPeople(date) { person ->
        personPersister.persistPerson(
          person,
          {
            saved.incrementAndGet()
            if (saved.get() % 500 == 0) logger.info("Persisted ${saved.get()} people...")
          },
          {
            errors.incrementAndGet()
            logger.warn("Error persisting person ${person.personId} - ${it.message}")
          }
        )
      }

      auditService.create(AuditableEvent.importReportEvent("people", date, saved.get() + errors.get(), saved.get()))

      raiseMonitoringAlertIf(
        errors.get() > 0,
        "people: persisted ${saved.get()} and ${errors.get()} errors for reporting feed date $date."
      )

      raiseMonitoringAlertIf(saved.get() == 0, "There were no people to persist for reporting feed date $date.")

      logger.info("Imported ${saved.get()} people with ${errors.get()} errors.")
    }
  }

  private fun importProfilesOn(date: LocalDate) {
    logger.info("Importing profiles for date: $date.")

    import {
      val (saved, errors) = AtomicInteger(0) to AtomicInteger(0)

      reportImporter.importProfiles(date) { profile ->
        personPersister.persistProfile(
          profile,
          {
            saved.incrementAndGet()
            if (saved.get() % 500 == 0) logger.info("Persisted ${saved.get()} profiles...")
          },
          {
            errors.incrementAndGet()
            logger.warn("Error persisting profile ${profile.profileId} - ${it.message}")
          }
        )
      }

      auditService.create(AuditableEvent.importReportEvent("profiles", date, saved.get() + errors.get(), saved.get()))

      raiseMonitoringAlertIf(
        errors.get() > 0,
        "profiles: persisted ${saved.get()} and ${errors.get()} errors for reporting feed date $date."
      )

      raiseMonitoringAlertIf(saved.get() == 0, "There were no profiles to persist for reporting feed date $date.")

      logger.info("Imported ${saved.get()} profiles with ${errors.get()} errors.")
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
