package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import java.time.LocalDate
import java.time.LocalDateTime

class ImportReportsDataTaskTest {

  private val importReportsService: ImportReportsService = mock()
  private val monitoringService: MonitoringService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2022, 6, 15, 0, 0) }

  @Test
  fun `move data is backdated when backdating enabled`() {
    whenever(importReportsService.dateOfLastImport()).thenReturn(LocalDate.of(2022, 6, 12))

    LockAssert.TestHelper.makeAllAssertsPass(true)

    ImportReportsDataTask(true, importReportsService, timeSource, monitoringService).execute()

    verify(importReportsService).dateOfLastImport()
    verify(importReportsService).importAllReportsOn(LocalDate.of(2022, 6, 13))
    verify(importReportsService).importAllReportsOn(LocalDate.of(2022, 6, 14))
    verifyNoInteractions(monitoringService)
  }

  @Test
  fun `move data is imported for yesterday only when backdating is disabled`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    ImportReportsDataTask(false, importReportsService, timeSource, monitoringService).execute()

    verify(importReportsService, never()).dateOfLastImport()
    verify(importReportsService).importAllReportsOn(timeSource.yesterday())
    verifyNoInteractions(monitoringService)
  }

  @Test
  fun `monitoring service is called when fails to lock the task for execution`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    ImportReportsDataTask(true, importReportsService, timeSource, monitoringService).execute()

    verifyNoInteractions(importReportsService)
    verify(monitoringService).capture("Unable to lock task 'Import reports data' for execution")
  }
}
