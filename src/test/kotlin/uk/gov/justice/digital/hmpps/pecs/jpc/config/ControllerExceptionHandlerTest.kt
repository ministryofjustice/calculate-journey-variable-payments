package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import javax.servlet.http.HttpServletRequest

internal class ControllerExceptionHandlerTest {

  private val monitoringService: MonitoringService = mock()
  private val request: HttpServletRequest = mock()
  private val handler: ControllerExceptionHandler = ControllerExceptionHandler(monitoringService)

  @Test
  internal fun `catch all other exceptions handler interactions`() {
    val response = handler.handleException(Exception("something unexpected has happened"))

    assertThat(response.viewName).isEqualTo("error")
    assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST)

    verify(monitoringService).capture("An unexpected error has occurred in the JPC application, see the logs for more details.")
  }

  @Test
  internal fun `catch access denied handler interactions`() {
    val response = handler.handleAccessDeniedException(AccessDeniedException("forbidden access"), request)

    assertThat(response.viewName).isEqualTo("error/403")
    assertThat(response.status).isEqualTo(HttpStatus.FORBIDDEN)

    verifyNoInteractions(monitoringService)
  }
}
