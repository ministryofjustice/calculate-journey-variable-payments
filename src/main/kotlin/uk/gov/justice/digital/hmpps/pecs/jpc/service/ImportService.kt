package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.location.LocationsImporter
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
        private val locationsImporter: LocationsImporter,
        private val priceImporter: PriceImporter,
        private val reportImporter: ReportImporter,
        private val movePersister: MovePersister,
        private val personPersister: PersonPersister) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun importLocations() = import(locationsImporter::import)

    fun importPrices(supplier: Supplier) = import { priceImporter.import(supplier) }

    fun importReports(reportsFrom: LocalDate, reportsTo: LocalDate) {
        importMovesJourneysEvents(reportsFrom, reportsTo)
        importPeopleProfiles(reportsFrom, reportsTo)
    }

    fun importMovesJourneysEvents(reportsFrom: LocalDate, reportsTo: LocalDate) {
        logger.info("Importing moves, journeys and events from '$reportsFrom' to '$reportsTo'.")
        val movesJourneysEvents = import { reportImporter.importMovesJourneysEvents(reportsFrom, reportsTo) }
        movesJourneysEvents?.let { movePersister.persist(it.toList()) }
    }

    fun importPeopleProfiles(reportsFrom: LocalDate, reportsTo: LocalDate) {
        logger.info("Importing people from '$reportsFrom' to '$reportsTo'.")
        val people = import { reportImporter.importPeople(reportsFrom, reportsTo) }
        people?.let{ personPersister.persistPeople(it.toList())}

        logger.info("Importing profiles from '$reportsFrom' to '$reportsTo'.")
        val profiles = import { reportImporter.importProfiles(reportsFrom, reportsTo) }
        profiles?.let{ personPersister.persistProfiles(it.toList())}
    }

    /**
     * @return a pair representing the return result of the importer, and an import status
     */
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
