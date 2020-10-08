package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired @Qualifier(value = "spreadsheet-template") private val template: File,
                                 @Autowired private val clock: Clock,
                                 @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                 @Autowired private val geoamyPricesProvider: GeoamyPricesProvider) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(filter: FilterParams, calculator: PriceCalculator): File {
        val dateGenerated = LocalDate.now(clock)

        XSSFWorkbook(template.inputStream()).use { workbook ->
            val header = PriceSheet.Header(dateGenerated, filter.dateRange(), filter.supplier)

            StandardMovesSheet(workbook, header)
                    .also { logger.info("Adding standard prices.") }
                    .apply { addPrices(calculator.standardPrices(filter)) }

            RedirectionMovesSheet(workbook, header)
                    .also { logger.info("Adding redirect prices.") }
                    .apply { addPrices(calculator.redirectionPrices(filter)) }
    
            LongHaulMovesSheet(workbook, header)
                    .also { logger.info("Adding long haul prices.") }
                    .apply { addPrices(calculator.longHaulPrices(filter)) }

            LockoutMovesSheet(workbook, header)
                    .also { logger.info("Adding long haul prices.") }
                    .apply { addPrices(calculator.lockoutPrices(filter)) }

            MultiTypeMovesSheet(workbook, header)
                    .also { logger.info("Adding multitype prices.") }
                    .apply { addPrices(calculator.multiTypePrices(filter)) }

            JPCPriceBookSheet(workbook)
                    .also { logger.info("Adding supplier JPC price book used.") }
                    .apply { copyPricesFrom(originalPricesSheetFor(header.supplier)) }

            return createTempFile(suffix = "xlsx").apply {
                FileOutputStream(this).use {
                    workbook.write(it)
                }
            }
        }
    }

    private fun originalPricesSheetFor(supplier: Supplier): Sheet {
        return when (supplier) {
            Supplier.GEOAMEY -> XSSFWorkbook(geoamyPricesProvider.get()).use { it.getSheetAt(0)!! }
            Supplier.SERCO -> XSSFWorkbook(sercoPricesProvider.get()).use { it.getSheetAt(0)!! }
        }
    }
}
