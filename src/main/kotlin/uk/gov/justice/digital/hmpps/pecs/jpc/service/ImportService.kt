package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculatorFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.output.PricesSpreadsheetGenerator
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveFiltererParams
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
        private val calculatorFactory: PriceCalculatorFactory,
        private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Imposing a temporary crude locking solution to prevent DB clashes/conflicts.  I know this will not scale!
    private val lock = AtomicBoolean(false)

    /**
     * Attempts to import using the importer passed in checking if there's an import in progress
     * @param importer - the importer to perform the actual import
     * @return a pair representing the return result of the importer or null if unsuccessful, and an import status
     */
    fun <T> importUnlessLocked(import: () -> T): Pair<T?, ImportStatus> {
        val statusAndResult = if (lock.compareAndSet(false, true)) {
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

        return statusAndResult
    }

    fun importLocations() = importUnlessLocked(locationsImporter::import)

    fun importPrices() = importUnlessLocked(priceImporter::import)

    fun spreadsheet(
            supplierName: String,
            movesFrom: LocalDate,
            movesTo: LocalDate,
            reportsTo: LocalDate): File? {

        return if (importLocations().second == ImportStatus.IN_PROGRESS) null
        else if (importPrices().second == ImportStatus.IN_PROGRESS) null
        else {
            val supplier = Supplier.valueOf(supplierName.toUpperCase())
            val (reports, status) = importUnlessLocked { reportingImporter.import(movesFrom, reportsTo) }
            if (reports != null) {
                val calculator = calculatorFactory.calculator(reports.toList())
                val prices = calculator.standardPrices(MoveFiltererParams(supplier, movesFrom, movesTo))
                pricesSpreadsheetGenerator.generateSpreadsheet(movesFrom, movesTo, supplier, prices)
            } else {
                null
            }
        }
    }
}
