package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.output.PricesSpreadsheetGenerator
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportingImporter
import java.io.File
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Service
class ImportService(
        private val clock: Clock,
        private val locationsImporter: LocationsImporter,
        private val priceImporter: PriceImporter,
        private val reportingImporter: ReportingImporter,
        private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator,
        private val locationsRepository: LocationRepository) {

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
            val start = LocalDateTime.now(clock)
            try {
                Pair(import(), ImportStatus.DONE)
            } finally {
                lock.set(false)
                val end = LocalDateTime.now(clock)
                logger.info("Import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
            }
        } else {
            logger.warn("Import already in progress.")
            Pair(null, ImportStatus.IN_PROGRESS)
        }
    }

    fun importLocations() = importUnlessLocked(locationsImporter::import)

    fun importPrices(supplier: Supplier) = importUnlessLocked { priceImporter.import(supplier) }

    fun spreadsheet(
            supplierName: String,
            movesFrom: LocalDate,
            movesTo: LocalDate,
            reportsTo: LocalDate): File? {

        return if (importLocations().second == ImportStatus.IN_PROGRESS) null
        else if (importPrices(Supplier.valueOf(supplierName.toUpperCase())).second == ImportStatus.IN_PROGRESS) null
        else {
            val supplier = Supplier.valueOf(supplierName.toUpperCase())
            val (reports, status) = importUnlessLocked { reportingImporter.import(movesFrom, reportsTo, locationsRepository.findAll().toList()) }
            if (reports != null) {
                pricesSpreadsheetGenerator.generate(FilterParams(supplier, movesFrom, movesTo), reports.toList())
            } else {
                null
            }
        }
    }
}
