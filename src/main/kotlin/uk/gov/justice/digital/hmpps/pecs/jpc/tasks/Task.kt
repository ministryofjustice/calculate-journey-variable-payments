package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import net.javacrumbs.shedlock.core.LockAssert
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService

abstract class Task(private val name: String, private val monitoringService: MonitoringService) {

  private val logger = LoggerFactory.getLogger(javaClass)

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
