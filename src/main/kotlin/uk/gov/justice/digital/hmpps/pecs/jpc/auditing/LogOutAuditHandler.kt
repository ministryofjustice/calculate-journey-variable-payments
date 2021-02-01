package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogOutAuditHandler(@Autowired private val auditService: AuditService) : LogoutHandler {
  override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
    authentication?.let { auditService.createLogOutEvent(it.name) }
  }
}
