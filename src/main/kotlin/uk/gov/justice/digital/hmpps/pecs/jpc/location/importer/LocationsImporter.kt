package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.io.FileInputStream

@Component
class LocationsImporter(private val repo: LocationRepository, private val eventPublisher: ApplicationEventPublisher) : InitializingBean {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(locationsWorkbook: XSSFWorkbook) : Set<String> {

        val errors: MutableSet<String?> = mutableSetOf()

        LocationType.values().iterator().forEach { locationType ->
            val sheet = locationType.sheet(locationsWorkbook)

            val rows = sheet.drop(1).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

            rows.forEach { row ->
                Result.runCatching { repo.save(locationType.toLocation(row)) }
                        .onFailure { errors.add(it.message + " " + it.cause.toString()) }
            }
        }

        logger.info("LOCATIONS INSERTED: ${repo.count()}")

        return errors.filterNotNull().sorted().toSet()
    }

     override fun afterPropertiesSet() {
        repo.deleteAll()

        val excelFile = FileInputStream(locationsFile)
        val workbook = XSSFWorkbook(excelFile)

        val errors = import(workbook)

        workbook.close()
        excelFile.close()

        eventPublisher.publishEvent(LocationsImportedEvent(this))
    }
}

class LocationsImportedEvent(source: Any) : ApplicationEvent(source)