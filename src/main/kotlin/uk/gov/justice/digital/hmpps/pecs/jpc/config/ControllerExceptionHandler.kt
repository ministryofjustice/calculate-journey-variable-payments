package uk.gov.justice.digital.hmpps.pecs.jpc.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<ControllerExceptionHandler>()

@ControllerAdvice
class ControllerExceptionHandler(private val monitoringService: MonitoringService) {

  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFound(e: ResourceNotFoundException): ModelAndView {
    logger.warn("Resource not found exception", e)

    return ModelAndView()
      .apply {
        this.viewName = "error/404"
        this.status = HttpStatus.NOT_FOUND
      }
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException, request: HttpServletRequest): ModelAndView {
    logger.warn("Access denied exception", e)

    return ModelAndView()
      .apply {
        this.viewName = "error/403"
        this.status = HttpStatus.FORBIDDEN
      }.also {
        SecurityContextHolder.getContext().authentication?.let {
          logger.warn("User: ${it.name} attempted to access the protected URL: ${request.requestURI}")
        }
      }
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ModelAndView {
    logger.error("Unexpected exception", e)

    return ModelAndView()
      .apply {
        this.viewName = "error"
        this.status = HttpStatus.BAD_REQUEST
      }
      .also { monitoringService.capture("An unexpected error has occurred in the JPC application, see the logs for more details.") }
  }
}

class ResourceNotFoundException(msg: String) : RuntimeException(msg)
