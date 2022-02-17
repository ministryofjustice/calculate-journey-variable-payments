package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.AutomaticLocationMappingService
import java.time.LocalDate
import java.time.LocalDateTime

internal class PreviousDaysLocationMappingTaskTest {

  private val mappingService: AutomaticLocationMappingService = mock()
  private val monitoringService: MonitoringService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }
  private val task = PreviousDaysLocationMappingTask(mappingService, timeSource, monitoringService)

  @Test
  internal fun `locations mapping invoked with previous days date`() {
    LockAssert.TestHelper.makeAllAssertsPass(true)

    task.execute()

    verify(mappingService).mapIfNotPresentLocationsCreatedOn(LocalDate.of(2020, 11, 29))
    verifyNoInteractions(monitoringService)
  }

  @Test
  internal fun `monitoring service is called when fails to lock the task for execution`() {
    LockAssert.TestHelper.makeAllAssertsPass(false)

    task.execute()

    verifyNoInteractions(mappingService)
    verify(monitoringService).capture("Unable to lock task 'Previous days locations' for execution")
  }
}
