package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService

class LogOutAuditHandler(
  private val auditService: AuditService,
  private val authLogoutSuccessUri: String,
) : SimpleUrlLogoutSuccessHandler() {
  override fun onLogoutSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?
  ) {
    auditService.create(AuditableEvent.logOutEvent(authentication!!))
    defaultTargetUrl = authLogoutSuccessUri
    super.onLogoutSuccess(request, response, authentication)
  }
}
