package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.SUPPLIER_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val logger = loggerFor<ChooseSupplierFilter>()

class ChooseSupplierFilter : Filter {

  @Throws(ServletException::class)
  override fun init(filterConfig: FilterConfig?) {
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val session = (request as HttpServletRequest).session

    when (session.getAttribute(SUPPLIER_ATTRIBUTE)) {
      null -> {
        logger.info("no supplier present in the session, redirecting to '${HtmlController.CHOOSE_SUPPLIER_URL}'")

        (response as HttpServletResponse).sendRedirect(HtmlController.CHOOSE_SUPPLIER_URL)
      }
      else -> chain.doFilter(request, response).also { logger.info("supplier present in the session") }
    }
  }

  override fun destroy() {}
}