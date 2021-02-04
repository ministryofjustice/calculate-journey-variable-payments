package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogInAuditHandler : AuthenticationSuccessHandler {
  @Autowired
  private lateinit var auditService: AuditService

  override fun onAuthenticationSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?
  ) {
    authentication?.let { auditService.createLogInEvent(it.name) }
    response?.sendRedirect("/")
  }
}
