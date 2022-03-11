package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveTypeSummaries
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate
import java.time.LocalDateTime

class ReprocessExistingMovesTaskTest {

  private val service: HistoricMovesProcessingService = mock()
  private val moveService: MoveService = mock { on { moveTypeSummaries(any(), any()) } doReturn MoveTypeSummaries(0, listOf()) }
  private val monitoringService: MonitoringService = mock()
  private val fixedTimeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 9, 30, 0, 0) }
  private val task = ReprocessExistingMovesTask(service, moveService, fixedTimeSource, monitoringService)

  @Test
  internal fun `processing service for Serco and GEO is invoked for September 2020`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verifyProcessingCalled(DateRange(EffectiveYear.startOfContract(), fixedTimeSource.yesterday()), Supplier.SERCO)
    verify(moveService, times(2)).moveTypeSummaries(Supplier.SERCO, EffectiveYear.startOfContract())

    verifyProcessingCalled(DateRange(EffectiveYear.startOfContract(), fixedTimeSource.yesterday()), Supplier.GEOAMEY)
    verify(moveService, times(2)).moveTypeSummaries(Supplier.GEOAMEY, EffectiveYear.startOfContract())

    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `processing service for Serco and GEO is invoked for September 2020 and October 2020`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    ReprocessExistingMovesTask(
      service,
      moveService,
      { LocalDateTime.of(2020, 11, 1, 0, 0) },
      monitoringService
    ).execute()

    verifyProcessingCalled(range(EffectiveYear.startOfContract(), date(2020, 9, 30)), Supplier.SERCO)
    verifyProcessingCalled(range(date(2020, 10, 1), date(2020, 10, 31)), Supplier.SERCO)
    verify(moveService, times(4)).moveTypeSummaries(eq(Supplier.SERCO), any())

    verifyProcessingCalled(range(EffectiveYear.startOfContract(), date(2020, 9, 30)), Supplier.GEOAMEY)
    verifyProcessingCalled(range(date(2020, 10, 1), date(2020, 10, 31)), Supplier.GEOAMEY)
    verify(moveService, times(4)).moveTypeSummaries(eq(Supplier.GEOAMEY), any())

    verifyNoInteractions(monitoringService)
  }

  private fun range(from: LocalDate, to: LocalDate) = DateRange(from, to)

  private fun date(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)

  private fun verifyProcessingCalled(range: DateRange, supplier: Supplier) {
    verify(service).process(range, supplier)
  }

  @Test
  internal fun `monitoring service is called when fails to lock the task for execution`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    task.execute()

    verifyNoInteractions(service)
    verify(monitoringService).capture("Unable to lock task 'Reprocess historic moves' for execution")
  }
}
