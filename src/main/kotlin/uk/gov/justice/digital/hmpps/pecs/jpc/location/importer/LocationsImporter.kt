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

private const val COLUMN_HEADINGS = 1

@Component
class LocationsImporter(private val repo: LocationRepository,
                        private val clock: Clock,
                        private val schedule34LocationsProvider: Schedule34LocationsProvider) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val rowOffset = 2
    private val running = AtomicBoolean(false)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(locationsWorkbook: XSSFWorkbook) {

        val errors: MutableList<LocationImportError?> = mutableListOf()

        var total = 0

        LocationTab.values().map { locationTab ->
            val sheet = locationTab.sheet(locationsWorkbook)

            val rows = sheet.drop(COLUMN_HEADINGS).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

            rows.forEachIndexed{ index, row ->
                run {
                    Result.runCatching {
                        locationTab.map(row).let {
                            repo.save(it)
                            total++
                        }
                    }.onFailure { errors.add(LocationImportError(locationTab, index + rowOffset, it.cause?.cause ?: it)) }
                }
            }
        }

        errors.filterNotNull().forEach { logger.info(it.toString()) }

        logger.info("LOCATIONS INSERTED: $total. TOTAL ERRORS: ${errors.size}")
    }

    fun import(): ImportStatus {
        // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
        if (running.compareAndSet(false, true)) {

            val start = LocalDateTime.now(clock)

            logger.info("Location data import started: $start")

            repo.deleteAll()

            try {
                schedule34LocationsProvider.get(locationsFile).use { locations ->
                    XSSFWorkbook(locations).use {
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
