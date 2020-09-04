package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.io.File
import java.io.FileInputStream

@Component
class LocationsImporter (private val repo: LocationRepository, private val eventPublisher: ApplicationEventPublisher) : InitializingBean {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(locationsFile: File){
        val excelFile = FileInputStream(locationsFile)
        val workbook = XSSFWorkbook(excelFile)

        val tabs = listOf(Court, Police, Prison, Hospital, Immigration, STCSCH, Other)
        tabs.forEach { tab ->
            val sheet = workbook.getSheetAt(tab.sheetIndex)
            val rows = sheet.iterator().asSequence().toList().
                drop(1).
                filter { r -> !r.getCell(1)?.stringCellValue.isNullOrBlank()
            }
            rows.forEach { r ->
                val rowCells = r.iterator().asSequence().toList()
                val location = tab.locationFromRowCells(rowCells)
                try {
                    repo.save(location)
                } catch (e: Exception) {
                }

            }
        }
        workbook.close()
        excelFile.close()
        logger.info("LOCATIONS INSERTED: ${repo.count()}")
    }

    override fun afterPropertiesSet() {
        repo.deleteAll()
        import(File(locationsFile))
        eventPublisher.publishEvent(LocationsImportedEvent(this))
    }
}

class LocationsImportedEvent(source: Any) : ApplicationEvent(source)