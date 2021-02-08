package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogInAuditHandler(@Autowired private val auditService: AuditService) :
  SavedRequestAwareAuthenticationSuccessHandler() {
  override fun onAuthenticationSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?
  ) {
    auditService.create(AuditableEvent.createLogInEvent(authentication))
    super.onAuthenticationSuccess(request, response, authentication)
  }
}
