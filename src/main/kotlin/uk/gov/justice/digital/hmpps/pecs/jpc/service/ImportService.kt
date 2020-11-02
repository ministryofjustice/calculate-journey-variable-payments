package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.output.PricesSpreadsheetGenerator
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModelPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportImporter
import java.io.File
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean

@Service
class ImportService(
        private val timeSource: TimeSource,
        private val locationsImporter: LocationsImporter,
        private val priceImporter: PriceImporter,
        private val reportImporter: ReportImporter,
        private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator,
        private val reportModelPersister: MoveModelPersister) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Imposing a temporary crude locking solution to prevent DB clashes/conflicts.  I know this will not scale!
    private val lock = AtomicBoolean(false)

    /**
     * Attempts to import using the importer passed in checking if there's an import in progress
     * @param import - the importer to perform the actual import
     * @return a pair representing the return result of the importer or null if unsuccessful, and an import status
     */
    fun <T> importUnlessLocked(import: () -> T): Pair<T?, ImportStatus> {
        return if (lock.compareAndSet(false, true)) {
            logger.info("Attempting import of ${import.javaClass}")
            val start = timeSource.dateTime()
            try {
                Pair(import(), ImportStatus.DONE)
            } finally {
                lock.set(false)
                val end = timeSource.dateTime()
                logger.info("Import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
            }
        } else {
            logger.warn("Import already in progress.")
            Pair(null, ImportStatus.IN_PROGRESS)
        }
    }

    fun importLocations() = importUnlessLocked(locationsImporter::import)

    fun importPrices(supplier: Supplier) = importUnlessLocked { priceImporter.import(supplier) }

    fun importReports(supplierName: String, reportsFrom: LocalDate, reportsTo: LocalDate) {

        logger.info("Importing reports for supplier '$supplierName', moves from '$reportsFrom', moves to '$reportsTo'.")

        val supplier = Supplier.valueOfCaseInsensitive(supplierName)
        val (reports, status) = importUnlessLocked { reportImporter.import(supplier, reportsFrom, reportsTo) }

        reports?.let{
            reportModelPersister.persist(FilterParams(supplier, reportsFrom, reportsTo), it.toList())
        }
    }

    fun spreadsheet(supplierName: String, movesFrom: LocalDate, movesTo: LocalDate): File? {

        logger.info("Generating spreadsheet for supplier '$supplierName', moves from '$movesFrom', moves to '$movesTo'")

        if (movesTo.plusDays(1).minusMonths(1).isAfter(movesFrom))
            throw IllegalArgumentException("A maximum of one month's data can be queried at a time.")

        return pricesSpreadsheetGenerator.generate(FilterParams(Supplier.valueOfCaseInsensitive(supplierName), movesFrom, movesTo))
    }
}
