package uk.gov.justice.digital.hmpps.pecs.jpc.filter

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.filter.ChooseSupplierFilter
import java.time.LocalDateTime
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

internal class ChooseSupplierFilterTest {

    private val session: HttpSession = mock()

    private val request: HttpServletRequest = mock()

    private val response: HttpServletResponse = mock()

    private val filterChain: FilterChain = mock()

    private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 18, 0, 0) }

    private val filter: ChooseSupplierFilter = ChooseSupplierFilter(timeSource)

    @Test
    internal fun `filter redirects to choose supplier when no supplier selected`() {
        whenever(request.session).thenReturn(session)
        whenever(session.getAttribute("supplier")).thenReturn(null)

        filter.doFilter(request, response, filterChain)

        verify(session).getAttribute("supplier")
        verify(session).setAttribute("date", timeSource.date().withDayOfMonth(1))
        verify(response).sendRedirect("/choose-supplier")
    }

    @Test
    internal fun `filter does not redirect when supplier already selected`() {
        whenever(request.session).thenReturn(session)
        whenever(session.getAttribute("supplier")).thenReturn("SERCO")

        filter.doFilter(request, response, filterChain)

        verify(session).getAttribute("supplier")
        verify(session, never()).setAttribute(any(), any())
        verify(response, never()).sendRedirect(any())
        verify(filterChain).doFilter(request, response)
    }
}
