package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired @Qualifier(value = "spreadsheet-template") private val template: File,
                                          @Autowired private val clock: Clock) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(filter: FilterParams, calculator: PriceCalculator): File {
        val dateGenerated = LocalDate.now(clock)

        XSSFWorkbook(template.inputStream()).use { workbook ->
            val header = PriceSheet.Header(dateGenerated, filter.dateRange(), filter.supplier)

            StandardMovesSheet(workbook, header)
                    .also { logger.info("Adding standard prices.") }
                    .apply { addPrices(calculator.standardPrices(filter)) }

            RedirectMovesSheet(workbook, header)
                    .also { logger.info("Adding redirect prices.") }
                    .apply { addPrices(calculator.redirectionPrices(filter)) }

            return createTempFile(suffix = "xlsx").apply {
                FileOutputStream(this).use {
                    workbook.write(it)
                }
            }
        }
    }
}
