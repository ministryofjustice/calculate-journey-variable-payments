package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import javax.validation.ValidationException

@RestControllerAdvice
class ExceptionHandler(private val monitoringService: MonitoringService) {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(status = HttpStatus.BAD_REQUEST))
  }

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Illegal argument exception: {}", e.message)

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(status = HttpStatus.BAD_REQUEST))
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)

    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR))
      .also { monitoringService.capture("An unexpected error has occurred in the JPC application, see the logs for more details.") }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val userMessage: String,
) {
  constructor(
    status: HttpStatus,
    userMessage: String = "An unexpected error has occurred with the JPC application, please contact support.",
  ) : this(status.value(), userMessage)
}
