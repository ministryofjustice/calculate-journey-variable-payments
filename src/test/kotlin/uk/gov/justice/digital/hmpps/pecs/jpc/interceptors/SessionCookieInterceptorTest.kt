package uk.gov.justice.digital.hmpps.pecs.jpc.interceptors

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Duration
import javax.servlet.http.Cookie

class SessionCookieInterceptorTest {

  private val sessionCookie: Cookie = Cookie("SESSION", "")

  private val nonSessionCookie: Cookie = Cookie("OTHER", "")

  private val response = MockHttpServletResponse()

  @Test
  fun `insecure session cookie values are set when intercepted`() {
    val inSecureRequest = requestWith(sessionCookie).apply { isSecure = false }

    SessionCookieInterceptor(SessionCookieProperties(Duration.ofMinutes(1), true)).postHandle(
      inSecureRequest,
      response,
      mock(),
      mock()
    )

    assertThat(response.cookies).contains(sessionCookie)
    assertThat(sessionCookie.path).isEqualTo("/")
    assertThat(sessionCookie.secure).isFalse
    assertThat(sessionCookie.isHttpOnly).isTrue
    assertThat(sessionCookie.maxAge).isEqualTo(60)
  }

  @Test
  fun `secure session cookie values are set when intercepted`() {
    val secureRequest = requestWith(sessionCookie).apply { isSecure = true }

    SessionCookieInterceptor(SessionCookieProperties(Duration.ofMinutes(2), false)).postHandle(
      secureRequest,
      response,
      mock(),
      mock()
    )

    assertThat(response.cookies).contains(sessionCookie)
    assertThat(sessionCookie.path).isEqualTo("/")
    assertThat(sessionCookie.secure).isTrue
    assertThat(sessionCookie.isHttpOnly).isFalse
    assertThat(sessionCookie.maxAge).isEqualTo(120)
  }

  @Test
  fun `non-session cookie is not intercepted`() {
    SessionCookieInterceptor(SessionCookieProperties(Duration.ofMinutes(1), true)).postHandle(
      requestWith(
        nonSessionCookie
      ),
      response, mock(), mock()
    )

    assertThat(response.cookies).doesNotContain(nonSessionCookie)
  }

  private fun requestWith(cookie: Cookie) = MockHttpServletRequest().apply { setCookies(cookie) }
}
