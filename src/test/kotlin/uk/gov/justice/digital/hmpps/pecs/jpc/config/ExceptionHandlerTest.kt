package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService

internal class ExceptionHandlerTest {

  private val monitoringService: MonitoringService = mock()
  private val handler: ExceptionHandler = ExceptionHandler(monitoringService)

  @Test
  internal fun `catch all other exceptions handler interactions`() {
    val response = handler.handleException(Exception("something unexpected has happened"))

    assertThat(response?.body).isEqualTo(
      ErrorResponse(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "An unexpected error has occurred with the JPC application, please contact support."
      )
    )

    verify(monitoringService).capture("An unexpected error has occurred in the JPC application, see the logs for more details.")
  }
}
