package uk.gov.justice.digital.hmpps.pecs.jpc.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

private const val SPRING_SESSION_COOKIE = "SESSION"
private const val ANY_PATH_STARTING_AT_ROOT = "/"

/**
 * Intercepts the session cookie to help with cookie attribute management/lifecycle.
 */
class SessionCookieInterceptor(private val properties: SessionCookieProperties) : HandlerInterceptor {

  override fun postHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
    modelAndView: ModelAndView?,
  ) {
    request.cookies?.firstOrNull { it.name == SPRING_SESSION_COOKIE }?.apply {
      this.maxAge = properties.maxAge.toSeconds().toInt()
      this.path = ANY_PATH_STARTING_AT_ROOT
      this.secure = properties.secure
      this.isHttpOnly = properties.httpOnly
    }?.also { response.addCookie(it) }
  }
}
