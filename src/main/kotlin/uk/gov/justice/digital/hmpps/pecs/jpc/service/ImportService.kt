package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.Duration
import java.time.LocalDate

@Service
@Transactional
class ImportService(
  private val timeSource: TimeSource,
  private val priceImporter: PriceImporter,
  private val reportImporter: ReportImporter,
  private val movePersister: MovePersister,
  private val personPersister: PersonPersister
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun importPrices(supplier: Supplier) = import { priceImporter.import(supplier) }

  fun importReports(reportsFrom: LocalDate, reportsTo: LocalDate) {
    importMovesJourneysEvents(reportsFrom, reportsTo)
    importPeopleProfiles(reportsFrom, reportsTo)
  }

  private fun importMovesJourneysEvents(reportsFrom: LocalDate, reportsTo: LocalDate) {
    logger.info("Importing moves, journeys and events from '$reportsFrom' to '$reportsTo'.")

    import { reportImporter.importMovesJourneysEvents(reportsFrom, reportsTo) }?.let {
      val total = movePersister.persist(it.toList())
    }
  }

  private fun importPeopleProfiles(reportsFrom: LocalDate, reportsTo: LocalDate) {
    logger.info("Importing people from '$reportsFrom' to '$reportsTo'.")

    import { reportImporter.importPeople(reportsFrom, reportsTo) }?.let {
      val total = personPersister.persistPeople(it.toList())
    }

    logger.info("Importing profiles from '$reportsFrom' to '$reportsTo'.")

    import { reportImporter.importProfiles(reportsFrom, reportsTo) }?.let {
      val total = personPersister.persistProfiles(it.toList())
    }
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
