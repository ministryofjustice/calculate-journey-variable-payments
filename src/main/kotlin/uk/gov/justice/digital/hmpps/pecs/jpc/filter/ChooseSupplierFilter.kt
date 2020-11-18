package uk.gov.justice.digital.hmpps.pecs.jpc.filter

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

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig?) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse
        val session = req.session
        val supplier = session.getAttribute("supplier")

        if (supplier == null) {
            session.setAttribute("date", timeSource.date().withDayOfMonth(1))
            res.sendRedirect("/choose-supplier")
        } else {
            chain.doFilter(request, response)
        }
    }

    override fun destroy() {
    }
}
