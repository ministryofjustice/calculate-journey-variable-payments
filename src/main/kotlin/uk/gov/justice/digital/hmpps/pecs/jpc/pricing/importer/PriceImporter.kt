package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImportedEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import java.io.File
import java.io.FileInputStream

@Component
class PriceImporter(private val locationRepo: LocationRepository, private val priceRepo: PriceRepository) : ApplicationListener<LocationsImportedEvent> {

    @Value("\${import-files.geo-prices}")
    private lateinit var geoPricesFile: String

    @Value("\${import-files.serco-prices}")
    private lateinit var sercoPricesFile: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun import(priceFile: File, supplier: String) {
        val excelFile = FileInputStream(priceFile)
        val workbook = XSSFWorkbook(excelFile)

        val tab = ExcelTab(locationRepo)
        val sheet = workbook.getSheetAt(tab.sheetIndex)
        val rows = sheet.iterator().asSequence().toList().drop(1).filter { r ->
            !r.getCell(1)?.stringCellValue.isNullOrBlank()
        }
        rows.forEach { r ->
            val rowCells = r.iterator().asSequence().toList()
            tab.priceFromRowCells(supplier, rowCells)?.let { priceRepo.save(it) }
        }

        workbook.close()
        excelFile.close()
        logger.info("$supplier PRICES INSERTED: ${priceRepo.count()}")
    }


    override fun onApplicationEvent(event: LocationsImportedEvent) {
        priceRepo.deleteAll()
        import(File(geoPricesFile), "GEOAMY")
        import(File(sercoPricesFile), "SERCO")
    }

}