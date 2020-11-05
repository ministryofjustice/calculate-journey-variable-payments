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
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveQueryRepository

import java.io.File
import java.io.FileOutputStream

@Component
class PricesSpreadsheetGenerator(@Autowired private val template: JPCTemplateProvider,
                                 @Autowired private val timeSource: TimeSource,
                                 @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                 @Autowired private val geoameyPricesProvider: GeoameyPricesProvider,
                                 @Autowired private val moveQueryRepository: MoveQueryRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun generate(filter: FilterParams): File {
        val dateGenerated = timeSource.date()

        XSSFWorkbook(template.get()).use { workbook ->

            val header = PriceSheet.Header(dateGenerated, filter.dateRange(), filter.supplier)

            val moves = moveQueryRepository.findSummaryForSupplierInDateRange(filter.supplier, filter.movesFrom, filter.movesTo)

            with(moves) {
                StandardMovesSheet(workbook, header)
                        .also { logger.info("Adding standard prices.") }
                        .apply { writeMoves(standard) }

                RedirectionMovesSheet(workbook, header)
                        .also { logger.info("Adding redirect prices.") }
                        .apply { writeMoves(redirection) }

                LongHaulMovesSheet(workbook, header)
                        .also { logger.info("Adding long haul prices.") }
                        .apply { writeMoves(longHaul) }

                LockoutMovesSheet(workbook, header)
                        .also { logger.info("Adding lockout prices.") }
                        .apply { writeMoves(lockout) }

                MultiTypeMovesSheet(workbook, header)
                        .also { logger.info("Adding multi-type prices.") }
                        .apply { writeMoves(multi) }

                CancelledMovesSheet(workbook, header)
                        .also { logger.info("Adding cancelled moves.") }
                        .apply { writeMoves(cancelled) }

                SummarySheet(workbook, header)
                        .also { logger.info("Adding summaries.") }
                        .apply { writeSummaries(moves) }

                JPCPriceBookSheet(workbook)
                        .also { logger.info("Adding supplier JPC price book used.") }
                        .apply { copyPricesFrom(originalPricesSheetFor(header.supplier)) }
            }

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