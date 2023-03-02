package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class ChooseSupplierFilterTest {

  private val session: HttpSession = mock()

  private val request: HttpServletRequest = mock()

  private val response: HttpServletResponse = mock()

  private val filterChain: FilterChain = mock()

  private val filter: ChooseSupplierFilter = ChooseSupplierFilter()

  @Test
  internal fun `filter redirects to choose supplier when no supplier selected for affected URIs`() {
    whenever(request.session).thenReturn(session)
    whenever(request.requestURI).thenReturn("/filter-should-redirect")
    whenever(session.getAttribute("supplier")).thenReturn(null)

    filter.doFilter(request, response, filterChain)

    verify(session).getAttribute("supplier")
    verify(session, never()).setAttribute(any(), any())
    verify(response).sendRedirect("/choose-supplier")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "/choose-supplier",
      "/choose-supplier/geoamey",
      "/choose-supplier/serco",
      "/generate-prices-spreadsheet",
      "stylesheet.css",
      "image.svg",
      "image.png",
      "icon.ico",
      "javascript.js",
      "/health",
      "/health/liveness",
      "/health/readiness",
      "/info",
    ]
  )
  internal fun `filter does not redirect to choose supplier when no supplier selected for excluded URIs`(uri: String) {
    whenever(request.session).thenReturn(session)
    whenever(request.requestURI).thenReturn(uri)
    whenever(session.getAttribute("supplier")).thenReturn(null)

    filter.doFilter(request, response, filterChain)

    verify(session, never()).getAttribute("supplier")
    verify(session, never()).setAttribute(any(), any())
    verifyNoInteractions(response)
  }

  @Test
  internal fun `filter does not redirect when supplier already selected`() {
    whenever(request.session).thenReturn(session)
    whenever(request.requestURI).thenReturn("/filter-should-not-redirect")
    whenever(session.getAttribute("supplier")).thenReturn("SERCO")

    filter.doFilter(request, response, filterChain)

    verify(session).getAttribute("supplier")
    verify(session, never()).setAttribute(any(), any())
    verify(response, never()).sendRedirect(any())
    verify(filterChain).doFilter(request, response)
  }
}
