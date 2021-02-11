package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogOutAuditHandler(
  private val auditService: AuditService,
  private val authLogoutSuccessUri: String,
  private val timeSource: TimeSource
) : SimpleUrlLogoutSuccessHandler() {
  override fun onLogoutSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?
  ) {
    AuditableEvent.createLogOutEvent(timeSource, authentication)?.let { auditService.create(it) }
    defaultTargetUrl = authLogoutSuccessUri
    super.onLogoutSuccess(request, response, authentication)
  }
}
