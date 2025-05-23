package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.JourneyPriceCatalogueController.Companion.GENERATE_PRICES_SPREADSHEET
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.SUPPLIER_ATTRIBUTE
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.SummaryPageController.Companion.CHOOSE_SUPPLIER_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.IOException

private val log = loggerFor<ChooseSupplierFilter>()

class ChooseSupplierFilter : OncePerRequestFilter() {

  private val allowedLinks = arrayOf(
    "/health",
    "/health/liveness",
    "/health/readiness",
    "/info",
    CHOOSE_SUPPLIER_URL,
    "$CHOOSE_SUPPLIER_URL/geoamey",
    "$CHOOSE_SUPPLIER_URL/serco",
    // Allowed otherwise result of redirect ends up in the downloaded file.
    GENERATE_PRICES_SPREADSHEET,
  )

  private val allowedStaticResources = arrayOf(
    ".css",
    ".svg",
    ".png",
    ".ico",
    ".js",
  )

  override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.isAllowedURI()

  private fun HttpServletRequest.isAllowedURI() = allowedLinks.any { this.requestURI == it } || allowedStaticResources.any { this.requestURI.endsWith(it) }

  @Throws(IOException::class, ServletException::class)
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    when (request.session.getAttribute(SUPPLIER_ATTRIBUTE)) {
      null -> {
        log.info("no supplier present in the session, redirecting to '$CHOOSE_SUPPLIER_URL'")

        response.sendRedirect(CHOOSE_SUPPLIER_URL)
      }
      else -> chain.doFilter(request, response).also { logger.debug("supplier present in the session") }
    }
  }
}
