package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import java.time.LocalDate

internal class BackfillReportsTaskTest {

  private val importReportsService: ImportReportsService = mock()

  private val timeSource: TimeSource = TimeSource { LocalDate.of(2022, 2, 7).atStartOfDay() }

  private val monitoringService: MonitoringService = mock()

  private val task = BackfillReportsTask(importReportsService, timeSource, monitoringService)

  @Test
  internal fun `given the task is executed successfully the reports importer is called the expected number of days`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verify(importReportsService, times(524)).importAllReportsOn(any())
  }

  @Test
  internal fun `given the task fails to obtain a lock successfully the monitoring service is called to report the failure`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    task.execute()

    verifyNoInteractions(importReportsService)
    verify(monitoringService).capture("Unable to lock task 'Backfill reports' for execution")
  }
}
