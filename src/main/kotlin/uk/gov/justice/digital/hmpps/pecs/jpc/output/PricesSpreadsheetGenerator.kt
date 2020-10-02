package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveFiltererParams
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired @Qualifier(value = "spreadsheet-template") private val template: File,
                                          @Autowired private val clock: Clock) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(dateRange: ClosedRange<LocalDate>, supplier: Supplier, calculator: PriceCalculator): File {

        val dateGenerated = LocalDate.now(clock)

        XSSFWorkbook(template).use { workbook ->
            StandardMovesSheet(workbook, PriceSheet.Header(dateGenerated, dateRange, supplier))
                    .also { logger.info("Adding standard prices.") }
                    .apply { add(calculator.standardPrices(MoveFiltererParams(supplier, dateRange.start, dateRange.endInclusive))) }

            return createTempFile(suffix = "xlsx").apply {
                FileOutputStream(this).use {
                    workbook.write(it)
                }
            }
        }
    }
}
