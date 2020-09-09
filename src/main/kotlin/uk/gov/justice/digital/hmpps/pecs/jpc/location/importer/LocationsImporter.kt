package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.io.FileInputStream

@Component
class LocationsImporter(private val repo: LocationRepository, private val eventPublisher: ApplicationEventPublisher)  {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(locationsWorkbook: XSSFWorkbook) : Set<String> {

        val locationsFromCells = listOf(Court, Police, Prison, Hospital, Immigration, STCSCH, Other)
        val errors: MutableSet<String?> = mutableSetOf()

        locationsFromCells.forEach { locationFromCells ->
            val sheet = locationFromCells.sheet(locationsWorkbook)
            val rows = sheet.drop(1).filter { !it.getCell(1)?.stringCellValue.isNullOrBlank() }

            rows.forEach {
                Result.runCatching { repo.save(locationFromCells.location(it.toList())) }
                        .onFailure { errors.add(it.message + " " + it.cause.toString()) }
            }
        }

        logger.info("LOCATIONS INSERTED: ${repo.count()}")

        return errors.filterNotNull().sorted().toSet()
    }

     fun afterPropertiesSet() {
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