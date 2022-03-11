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
import java.time.LocalDate
import java.time.LocalDateTime

class ReprocessExistingMovesTaskTest {

  private val service: HistoricMovesProcessingService = mock()
  private val monitoringService: MonitoringService = mock()
  private val fixedTimeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 9, 30, 0, 0) }
  private val task = ReprocessExistingMovesTask(service, fixedTimeSource, monitoringService)

  @Test
  internal fun `processing service for Serco and GEO is invoked for September 2020`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verifyCalledWith(DateRange(EffectiveYear.startOfContract(), fixedTimeSource.yesterday()), Supplier.SERCO)
    verifyCalledWith(DateRange(EffectiveYear.startOfContract(), fixedTimeSource.yesterday()), Supplier.GEOAMEY)
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `processing service for Serco and GEO is invoked for September 2020 and October 2020`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    ReprocessExistingMovesTask(
      service,
      { LocalDateTime.of(2020, 11, 1, 0, 0) },
      monitoringService
    ).execute()

    verifyCalledWith(range(EffectiveYear.startOfContract(), date(2020, 9, 30)), Supplier.SERCO)
    verifyCalledWith(range(EffectiveYear.startOfContract(), date(2020, 9, 30)), Supplier.GEOAMEY)
    verifyCalledWith(range(date(2020, 10, 1), date(2020, 10, 31)), Supplier.SERCO)
    verifyCalledWith(range(date(2020, 10, 1), date(2020, 10, 31)), Supplier.GEOAMEY)

    verifyNoInteractions(monitoringService)
  }

  private fun range(from: LocalDate, to: LocalDate) = DateRange(from, to)

  private fun date(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)

  private fun verifyCalledWith(range: DateRange, supplier: Supplier) {
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
