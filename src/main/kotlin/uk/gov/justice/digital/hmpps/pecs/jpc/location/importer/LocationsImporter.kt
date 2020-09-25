package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class LocationsImporter(private val locationRepo: LocationRepository,
                        private val clock: Clock,
                        private val schedule34LocationsProvider: Schedule34LocationsProvider) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val running = AtomicBoolean(false)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(spreadsheet: LocationsSpreadsheet) {
        val count = locationRepo.count();

        LocationsSpreadsheet.Tab.values().forEach { tab ->
            spreadsheet.getRowsFrom(tab).forEach { row ->
                Result.runCatching { spreadsheet.mapToLocation(row).let { locationRepo.save(it) } }.onFailure { spreadsheet.addError(tab, row, it) }
            }
        }

        spreadsheet.errors.forEach { logger.info(it.toString()) }

        val inserted = locationRepo.count() - count

        logger.info("LOCATIONS INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
    }

    fun import(): ImportStatus {
        // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
        if (running.compareAndSet(false, true)) {

            val start = LocalDateTime.now(clock)

            logger.info("Location data import started: $start")

            locationRepo.deleteAll()

            try {
                schedule34LocationsProvider.get(locationsFile).use { locations ->
                    LocationsSpreadsheet(XSSFWorkbook(locations)).use {
                        import(it)

                        return ImportStatus.DONE
                    }
                }
            } finally {
                running.set(false)

                val end = LocalDateTime.now(clock)

                logger.info("Location import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
            }
        }

        logger.info("Import already in progress...")

        return ImportStatus.IN_PROGRESS
    }
}
