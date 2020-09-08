package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.io.File
import java.io.FileInputStream

@Component
class LocationsImporter(private val repo: LocationRepository, private val eventPublisher: ApplicationEventPublisher)  {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(workbook: XSSFWorkbook) : Set<String> {

        val locationsFromCells = listOf(Court, Police, Prison, Hospital, Immigration, STCSCH, Other)
        val errors: MutableSet<String?> = mutableSetOf()
        locationsFromCells.forEach { locationFromCells ->
            val sheet = workbook.getSheetAt(locationFromCells.sheetIndex)
            val rows = sheet.iterator().asSequence().toList().drop(1).filter { r ->
                !r.getCell(1)?.stringCellValue.isNullOrBlank()
            }
            rows.forEach { r ->
                val rowCells = r.iterator().asSequence().toList()
                val locationResult = locationFromCells.getLocationResult(rowCells)

                locationResult.fold({ location ->
                    try {
                        repo.save(location)
                    } catch (e: Exception) {
                        when(e) {
                            is DataIntegrityViolationException -> {
                                // TODO collect these errors
                            }
                            else -> errors.add(e.message+" "+e)
                        }
                    }
                }, { error -> errors.add(error.message + " " + error.cause.toString())
                })

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