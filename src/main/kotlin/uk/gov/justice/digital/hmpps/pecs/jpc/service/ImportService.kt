package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.import.location.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.import.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveModelPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.Duration
import java.time.LocalDate

@Service
class ImportService(
        private val timeSource: TimeSource,
        private val locationsImporter: LocationsImporter,
        private val priceImporter: PriceImporter,
        private val reportImporter: ReportImporter,
        private val reportModelPersister: MoveModelPersister) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun importLocations() = import(locationsImporter::import)

    fun importPrices(supplier: Supplier) = import { priceImporter.import(supplier) }

    fun importReports(supplier: Supplier, reportsFrom: LocalDate, reportsTo: LocalDate) {
        logger.info("Importing reports for supplier '$supplier', moves from '$reportsFrom', moves to '$reportsTo'.")

        val reports = import { reportImporter.import(supplier, reportsFrom, reportsTo) }

        reports?.let {
            reportModelPersister.persist(FilterParams(supplier, reportsFrom, reportsTo), it.toList())
        }
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
