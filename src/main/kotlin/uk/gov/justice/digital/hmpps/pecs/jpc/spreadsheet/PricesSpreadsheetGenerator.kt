package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MovesForMonthService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth

import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(@Autowired private val template: JPCTemplateProvider,
                                 @Autowired private val timeSource: TimeSource,
                                 @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                 @Autowired private val geoameyPricesProvider: GeoameyPricesProvider,
                                 @Autowired private val movesForMonthService: MovesForMonthService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(supplier: Supplier, startDate: LocalDate): File {
        val dateGenerated = timeSource.date()

        XSSFWorkbook(template.get()).use { workbook ->

            val header = PriceSheet.Header(dateGenerated, ClosedRangeLocalDate(startDate, endOfMonth(startDate)), supplier)

            val moves = movesForMonthService.moves(supplier, startDate)
            val summaries = movesForMonthService.moveTypeSummaries(supplier, startDate)

                StandardMovesSheet(workbook, header)
                        .also { logger.info("Adding standard prices.") }
                        .apply { writeMoves(moves[0]) }

                RedirectionMovesSheet(workbook, header)
                        .also { logger.info("Adding redirect prices.") }
                        .apply { writeMoves(moves[1]) }

                LongHaulMovesSheet(workbook, header)
                        .also { logger.info("Adding long haul prices.") }
                        .apply { writeMoves(moves[2]) }

                LockoutMovesSheet(workbook, header)
                        .also { logger.info("Adding lockout prices.") }
                        .apply { writeMoves(moves[3]) }

                MultiTypeMovesSheet(workbook, header)
                        .also { logger.info("Adding multi-type prices.") }
                        .apply { writeMoves(moves[4]) }

                CancelledMovesSheet(workbook, header)
                        .also { logger.info("Adding cancelled moves.") }
                        .apply { writeMoves(moves[5]) }

                SummarySheet(workbook, header)
                        .also { logger.info("Adding summaries.") }
                        .apply { writeSummaries(summaries) }

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
            Supplier.GEOAMEY -> XSSFWorkbook(geoameyPricesProvider.get()).use { it.getSheetAt(0)!! }
            Supplier.SERCO -> XSSFWorkbook(sercoPricesProvider.get()).use { it.getSheetAt(0)!! }
        }
    }
}
