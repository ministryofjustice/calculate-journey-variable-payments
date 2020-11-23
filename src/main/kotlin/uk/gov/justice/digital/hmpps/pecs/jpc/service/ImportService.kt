package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.location.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.Duration
import java.time.LocalDate

@Service
class ImportService(
        private val timeSource: TimeSource,
        private val locationsImporter: LocationsImporter,
        private val priceImporter: PriceImporter,
        private val reportImporter: ReportImporter,
        private val reportPersister: MovePersister) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun importLocations() = import(locationsImporter::import)

    fun importPrices(supplier: Supplier) = import { priceImporter.import(supplier) }

    fun importReports(reportsFrom: LocalDate, reportsTo: LocalDate) {
        logger.info("Importing reports from '$reportsFrom', moves to '$reportsTo'.")

        val reports = import { reportImporter.import(reportsFrom, reportsTo) }

        reports?.let { reportPersister.persist(it.toList()) }
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
