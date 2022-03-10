package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDateTime

class ReprocessExistingMovesTaskTest {

  private val historicMovesProcessingService: HistoricMovesProcessingService = mock()
  private val monitoringService: MonitoringService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 9, 30, 0, 0) }
  private val task = ReprocessExistingMovesTask(historicMovesProcessingService, timeSource, monitoringService)

  @Test
  internal fun `processing service for Serco and GEO is invoked for September 2020`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verify(historicMovesProcessingService).process(
      DateRange(EffectiveYear.startOfContract(), timeSource.yesterday()), Supplier.SERCO
    )
    verify(historicMovesProcessingService).process(
      DateRange(EffectiveYear.startOfContract(), timeSource.yesterday()), Supplier.GEOAMEY
    )
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when fails to lock the task for execution`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    task.execute()

    verifyNoInteractions(historicMovesProcessingService)
    verify(monitoringService).capture("Unable to lock task 'Reprocess historic moves' for execution")
  }
}
