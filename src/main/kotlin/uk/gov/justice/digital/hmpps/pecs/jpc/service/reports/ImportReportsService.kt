package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportLookup
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType.REPORTING_DATA_IMPORT
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration
import java.time.LocalDate
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
  private val monitoringService: MonitoringService,
  private val reportLookup: ReportLookup,
) {

  /**
   * Null implies there are no imports.
   */
  fun dateOfLastImport(): LocalDate? =
    auditService.findMostRecentEventByType(REPORTING_DATA_IMPORT)?.createdAt?.toLocalDate()

  /**
   * A runtime exception will be thrown if the date is in the past or if any of the report files for the given date are
   * missing prior to importing.
   */
  fun importAllReportsOn(date: LocalDate) {
    failIfDateOfImportNotInPast(date)
    failIfAnyReportFilesAreMissingOn(date)

    val maybeMoves = importMovesJourneysEventsOn(date)
    val maybePeople = importPeopleOn(date)
    val maybeProfiles = importProfilesOn(date)

    if (maybeMoves != null && maybePeople != null && maybeProfiles != null) {
      auditService.create(
        AuditableEvent.importReportsEvent(
          reportDate = date,
          moves_processed = maybeMoves.processed,
          moves_saved = maybeMoves.saved,
          people_processed = maybePeople.processed,
          people_saved = maybePeople.saved,
          profiles_processed = maybeProfiles.processed,
          profiles_saved = maybeProfiles.saved,
        ),
      )
    }
  }

  private fun failIfDateOfImportNotInPast(date: LocalDate) {
    if (date.isAfter(timeSource.yesterday())) throw RuntimeException("Import date must be in the past.")
  }

  private fun failIfAnyReportFilesAreMissingOn(date: LocalDate) {
    ReportImporter.reportFilenamesFor(date)
      .mapNotNull { if (!reportLookup.doesReportExist(it)) it else null }
      .run {
        if (this.isNotEmpty()) {
          val message =
            "The service is missing data which may affect pricing due to missing file(s): ${this.joinToString(separator = ", ")}"
          logger.error(message)
          monitoringService.capture(message)

          throw RuntimeException(message)
        }
      }
  }

  private fun importMovesJourneysEventsOn(date: LocalDate): Totals? {
    logger.info("Importing moves, journeys and events for date: $date.")

    return import { reportImporter.importMovesJourneysEventsOn(date) }?.let {
      val moves = it.toList()
      val persisted = movePersister.persist(moves).let { persisted ->
        raiseMonitoringAlertIf(
          moves.isNotEmpty() && moves.size > persisted,
          "moves: persisted $persisted out of ${moves.size} for reporting feed date $date.",
        )

        raiseMonitoringAlertIf(moves.isEmpty(), "There were no moves to persist for reporting feed date $date.")

        persisted
      }

      Totals(moves.size, persisted)
    }
  }

  private fun importPeopleOn(date: LocalDate): Totals? {
    logger.info("Importing people for date: $date.")

    return import {
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
          },
        )
      }

      raiseMonitoringAlertIf(
        errors.get() > 0,
        "people: persisted ${saved.get()} and ${errors.get()} errors for reporting feed date $date.",
      )

      raiseMonitoringAlertIf(saved.get() == 0, "There were no people to persist for reporting feed date $date.")

      logger.info("Imported ${saved.get()} people with ${errors.get()} errors.")

      Totals(saved.get() + errors.get(), saved.get())
    }
  }

  private fun importProfilesOn(date: LocalDate): Totals? {
    logger.info("Importing profiles for date: $date.")

    return import {
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
          },
        )
      }

      raiseMonitoringAlertIf(
        errors.get() > 0,
        "profiles: persisted ${saved.get()} and ${errors.get()} errors for reporting feed date $date.",
      )

      raiseMonitoringAlertIf(saved.get() == 0, "There were no profiles to persist for reporting feed date $date.")

      logger.info("Imported ${saved.get()} profiles with ${errors.get()} errors.")

      Totals(saved.get() + errors.get(), saved.get())
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

private data class Totals(val processed: Int, val saved: Int)
