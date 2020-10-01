package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired @Qualifier(value = "spreadsheet-template") private val template: File,
                                 @Autowired private val clock: Clock) {

    fun generateSpreadsheet(from: LocalDate, to: LocalDate, supplier: Supplier, prices: Sequence<MovePrice>): File {
        val dateRun = LocalDate.now(clock)

        XSSFWorkbook(template).use { workbook ->
            StandardMovesSheet(workbook, PriceSheet.Header(dateRun, PriceSheet.DateRange(from, to), supplier)).apply { add(prices) }

            return createTempFile(suffix = "xlsx").apply {
                FileOutputStream(this).use {
                    workbook.write(it)
                }
            }
        }
    }
}
