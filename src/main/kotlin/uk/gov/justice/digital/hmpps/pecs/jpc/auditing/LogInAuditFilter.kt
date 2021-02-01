package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogInAuditFilter(
  private val timeSource: TimeSource,
  @Autowired private val auditService: AuditService
) : OncePerRequestFilter() {
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    if (request.session.getAttribute("loggedInAt") == null) {
      SecurityContextHolder.getContext().authentication?.let {
        request.session.setAttribute("loggedInAt", timeSource.dateTime())

        auditService.createLogInEvent(it.name)
      }
    }

    filterChain.doFilter(request, response)
  }
}
