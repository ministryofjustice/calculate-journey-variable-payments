package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SupplierPrices
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.endOfMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

private val logger = loggerFor<PricesSpreadsheetGenerator>()

@Component
class PricesSpreadsheetGenerator(
  @Autowired private val timeSource: TimeSource,
  @Autowired private val moveService: MoveService,
  @Autowired private val journeyService: JourneyService,
  @Autowired private val locationRepository: LocationRepository,
  @Autowired private val supplierPrices: SupplierPrices
) {

  internal fun generate(supplier: Supplier, startDate: LocalDate): File {
    val dateGenerated = timeSource.date()

    SXSSFWorkbook().use { workbook ->
      val header = PriceSheet.Header(dateGenerated, DateRange(startDate, endOfMonth(startDate)), supplier)

      SummarySheet(workbook, header).also { logger.info("Adding summaries.") }
        .apply { writeSummaries(moveService.moveTypeSummaries(supplier, startDate)) }

      StandardMovesSheet(workbook, header).also { logger.info("Adding standard prices.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.STANDARD, startDate)) }

      RedirectionMovesSheet(workbook, header).also { logger.info("Adding redirect prices.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.REDIRECTION, startDate)) }

      LongHaulMovesSheet(workbook, header).also { logger.info("Adding long haul prices.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.LONG_HAUL, startDate)) }

      LockoutMovesSheet(workbook, header).also { logger.info("Adding lockout prices.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.LOCKOUT, startDate)) }

      MultiTypeMovesSheet(workbook, header).also { logger.info("Adding multi-type prices.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.MULTI, startDate)) }

      CancelledMovesSheet(workbook, header).also { logger.info("Adding cancelled moves.") }
        .apply { writeMoves(moveService.movesForMoveType(supplier, MoveType.CANCELLED, startDate)) }

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
