package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<Task>()

abstract class Task(private val name: String, private val monitoringService: MonitoringService) {

  fun execute() {
    Result.runCatching {
      LockAssert.assertLocked()
    }
      .onSuccess { performTask() }
      .onFailure {
        logger.error("Unable to lock task '$name' for execution", it)
        monitoringService.capture("Unable to lock task '$name' for execution")
      }
  }

  protected abstract fun performTask()
}
