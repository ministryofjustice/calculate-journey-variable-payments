package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SupplierPrices
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@Component
class PricesSpreadsheetGenerator(
  @Autowired private val timeSource: TimeSource,
  @Autowired private val moveService: MoveService,
  @Autowired private val journeyService: JourneyService,
  @Autowired private val locationRepository: LocationRepository,
  @Autowired private val supplierPrices: SupplierPrices
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  internal fun generate(supplier: Supplier, startDate: LocalDate): File {
    val dateGenerated = timeSource.date()

    SXSSFWorkbook().use { workbook ->
      val header = PriceSheet.Header(dateGenerated, ClosedRangeLocalDate(startDate, endOfMonth(startDate)), supplier)

      val moves = moveService.moves(supplier, startDate)

      SummarySheet(workbook, header).also { logger.info("Adding summaries.") }
        .apply { writeSummaries(moveService.moveTypeSummaries(supplier, startDate)) }

      StandardMovesSheet(workbook, header).also { logger.info("Adding standard prices.") }
        .apply { writeMoves(moves[0]) }

      RedirectionMovesSheet(workbook, header).also { logger.info("Adding redirect prices.") }
        .apply { writeMoves(moves[1]) }

      LongHaulMovesSheet(workbook, header).also { logger.info("Adding long haul prices.") }
        .apply { writeMoves(moves[2]) }

      LockoutMovesSheet(workbook, header).also { logger.info("Adding lockout prices.") }.apply { writeMoves(moves[3]) }

      MultiTypeMovesSheet(workbook, header).also { logger.info("Adding multi-type prices.") }
        .apply { writeMoves(moves[4]) }

      CancelledMovesSheet(workbook, header).also { logger.info("Adding cancelled moves.") }
        .apply { writeMoves(moves[5]) }

      JourneysSheet(workbook, header).also { logger.info("Adding journeys.") }
        .apply { writeJourneys(journeyService.distinctJourneysIncludingPriced(supplier, startDate)) }

      SupplierPricesSheet(
        workbook,
        header
      ).also {
        logger.info("Adding $supplier prices for effective year ${effectiveYearForDate(startDate)}.")
      }.apply { write(supplierPrices.get(supplier, effectiveYearForDate(startDate))) }

      LocationsSheet(workbook, header).also { logger.info("Adding locations.") }
        .apply { write(locationRepository.findAllByOrderBySiteName()) }

      return File.createTempFile("tmp", "xlsx").apply {
        FileOutputStream(this).use {
          workbook.write(it)
        }
      }
    }
  }
}
