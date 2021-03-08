package uk.gov.justice.digital.hmpps.pecs.jpc.filter

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ChooseSupplierFilter(private val timeSource: TimeSource) : Filter {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Throws(ServletException::class)
  override fun init(filterConfig: FilterConfig?) {
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val req = request as HttpServletRequest
    val res = response as HttpServletResponse
    val session = req.session

    when (session.getAttribute("supplier")) {
      null -> {
        logger.info("no supplier present in the session, redirecting to choose supplier")

        session.setAttribute("date", timeSource.date().withDayOfMonth(1))
        res.sendRedirect("/choose-supplier")
      }
      else -> chain.doFilter(request, response).also { logger.info("supplier present in the session") }
    }
  }

  override fun destroy() {}
}
