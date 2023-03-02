package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService

class LogInAuditHandler(private val auditService: AuditService) :
  SavedRequestAwareAuthenticationSuccessHandler() {
  override fun onAuthenticationSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?
  ) {
    auditService.create(AuditableEvent.logInEvent(authentication!!))
    super.onAuthenticationSuccess(request, response, authentication)
  }
}
