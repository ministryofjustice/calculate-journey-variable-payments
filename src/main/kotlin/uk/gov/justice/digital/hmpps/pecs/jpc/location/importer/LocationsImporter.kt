package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ResourceProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class LocationsImporter(private val repo: LocationRepository,
                        private val clock: Clock) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val running = AtomicBoolean(false)

    @Autowired
    @Qualifier("locations")
    private lateinit var resourceLoader: ResourceProvider

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(locationsWorkbook: XSSFWorkbook) {

        val errors: MutableList<String?> = mutableListOf()

        var total = 0

        LocationType.values().iterator().forEach { locationType ->
            val sheet = locationType.sheet(locationsWorkbook)

            val rows = sheet.drop(1).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

            rows.forEach { row ->
                total++
                Result.runCatching { repo.save(locationType.toLocation(row)) }
                        .onFailure { errors.add(it.message + " " + it.cause.toString()) }
            }
        }

        errors.filterNotNull().sorted().forEach { logger.info(it) }

        logger.info("LOCATIONS INSERTED: ${total - errors.size} out of $total.")
    }

    fun import(): ImportStatus {
        // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
        if (running.compareAndSet(false, true)) {

            val start = LocalDateTime.now(clock)

            logger.info("Location data import started: $start")

            repo.deleteAll()

            try {
                XSSFWorkbook(resourceLoader.get(locationsFile)).use {
                    import(it)

                    return ImportStatus.DONE
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
