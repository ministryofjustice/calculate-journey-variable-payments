package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * The purpose of this task is go through all the historic moves from the start of the contract and reprocess them with
 * the view corrections are needed due a change in either functionality or bug has been fixed in the way the moves are
 * categorised.
 *
 * Even though it will be going through all the historic moves it will do it on a month by month basis to prevent
 * processing large quantities of data.
 */

private val logger = loggerFor<ReprocessExistingMovesTask>()

@Component
class ReprocessExistingMovesTask(
  private val processingService: HistoricMovesProcessingService,
  private val moveService: MoveService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Reprocess historic moves", monitoringService) {

  override fun performTask() {
    val dateRange = DateRange(EffectiveYear.startOfContract(), timeSource.yesterday())

    Supplier.forEach { supplier ->
      logger.info("Reprocessing historic moves for $supplier")

      logSupplierMoveSummaries(supplier, dateRange)

      dateRange.forEachMonth { month -> processingService.process(month, supplier) }

      logSupplierMoveSummaries(supplier, dateRange)

      logger.info("Finished reprocessing historic moves for $supplier")
    }
  }

  private fun logSupplierMoveSummaries(supplier: Supplier, range: DateRange) {
    range.forEachMonth {
      moveService.moveTypeSummaries(supplier, it.start).movesSummaries.forEach { summary ->
        logger.info("$supplier has ${summary.volume} ${summary.moveType} moves for the period ${it.start} to ${it.endInclusive}")
      }
    }
  }

  private fun DateRange.forEachMonth(consumer: (DateRange) -> Unit) = listOf().forEach { consumer(it) }
}
