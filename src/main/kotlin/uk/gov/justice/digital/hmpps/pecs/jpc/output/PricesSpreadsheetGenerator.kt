package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.withType
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JCPTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Report
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired private val template: JCPTemplateProvider,
                                 @Autowired private val clock: Clock,
                                 @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                 @Autowired private val geoamyPricesProvider: GeoamyPricesProvider,
                                 @Autowired private val calculator: PriceCalculator) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(filter: FilterParams, moves: List<Report>): File {
        val dateGenerated = LocalDate.now(clock)

        XSSFWorkbook(template.get()).use { workbook ->
            val header = PriceSheet.Header(dateGenerated, filter.dateRange(), filter.supplier)

            val allPrices = calculator.allPrices(filter, moves)
            fun PriceSheet.add(type: MovePriceType) = writeMoves(allPrices.withType(type).prices)

            StandardMovesSheet(workbook, header)
                    .also { logger.info("Adding standard prices.") }
                    .apply { add(MovePriceType.STANDARD) }

            RedirectionMovesSheet(workbook, header)
                    .also { logger.info("Adding redirect prices.") }
                    .apply { add(MovePriceType.REDIRECTION) }
    
            LongHaulMovesSheet(workbook, header)
                    .also { logger.info("Adding long haul prices.") }
                    .apply { add(MovePriceType.LONG_HAUL) }

            LockoutMovesSheet(workbook, header)
                    .also { logger.info("Adding lockout prices.") }
                    .apply { add(MovePriceType.LOCKOUT) }

            MultiTypeMovesSheet(workbook, header)
                    .also { logger.info("Adding multi-type prices.") }
                    .apply { add(MovePriceType.MULTI) }


            SummarySheet(workbook, header)
                    .also { logger.info("Adding summaries.") }
                    .apply { writeSummaries(allPrices.map{it.summary}) }

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
