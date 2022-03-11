package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange

/**
 * The purpose of this task is go through all the historic moves from the start of the contract and reprocess them with
 * the view corrections are needed due a change in either functionality or bug has been fixed in the way the moves are
 * categorised.
 *
 * Even though it will be going through all the historic moves it will do it on a month by month basis to prevent
 * processing large quantities of data.
 */
@Component
class ReprocessExistingMovesTask(
  private val service: HistoricMovesProcessingService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService
) : Task("Reprocess historic moves", monitoringService) {

  override fun performTask() {
    DateRange(EffectiveYear.startOfContract(), timeSource.yesterday()).forEachMonth { month ->
      Supplier.forEach { supplier -> service.process(month, supplier) }
    }
  }

  private fun DateRange.forEachMonth(consumer: (DateRange) -> Unit) = listOf().forEach { consumer(it) }
}
