package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService

@ControllerAdvice
class ExceptionHandler(private val monitoringService: MonitoringService) {
  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ModelAndView {
    log.error("Unexpected exception", e)

    return ModelAndView()
      .apply { this.viewName = "error" }
      .also { monitoringService.capture("An unexpected error has occurred in the JPC application, see the logs for more details.") }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
