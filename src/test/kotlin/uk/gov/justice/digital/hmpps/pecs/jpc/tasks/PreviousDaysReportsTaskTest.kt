package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.LocalDateTime

internal class PreviousDaysReportsTaskTest {

  private val importService: ImportService = mock()
  private val monitoringService: MonitoringService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }
  private val task = PreviousDaysReportsTask(importService, timeSource, monitoringService)

  @Test
  internal fun `reports data import invoked with previous days date`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verify(importService).importReportsOn(timeSource.date().minusDays(1))
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when fails to lock the task for execution`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    task.execute()

    verifyZeroInteractions(importService)
    verify(monitoringService).capture("Unable to lock task 'Previous Days Reports' for execution")
  }
}
