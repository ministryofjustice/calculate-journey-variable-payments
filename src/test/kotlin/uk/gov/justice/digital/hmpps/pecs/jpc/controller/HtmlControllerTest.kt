package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveTypeSummaries
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.defaultSupplierSerco
import java.time.LocalDate
import java.util.Optional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestPropertySource(properties = ["FEEDBACK_URL=http://fake_feeback_url_url"])
class HtmlControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  private val mockSession = MockHttpSession(wac.servletContext)

  @MockBean
  lateinit var moveService: MoveService

  @MockBean
  lateinit var monitoringService: MonitoringService

  @BeforeEach
  fun beforeEach() {
    mockSession.setAttribute("supplier", defaultSupplierSerco)
  }

  @Test
  internal fun `feedback URL is available upon navigation to the dashboard`() {
    mockSession.setAttribute("date", LocalDate.now())
    whenever(moveService.moveTypeSummaries(eq(defaultSupplierSerco), any())).thenReturn(MoveTypeSummaries(0, listOf()))

    mockMvc.get("/dashboard") { session = mockSession }
      .andExpect { model { attribute("feedbackUrl", "http://fake_feeback_url_url") } }
      .andExpect { status { isOk() } }
  }

  @Test
  internal fun `GET move with valid Move ID for supplier`() {
    val move = moveM1()
    whenever(moveService.moveWithPersonJourneysAndEvents(move.moveId, defaultSupplierSerco)).thenReturn(move)

    mockMvc.get("/moves/${move.moveId}") { session = mockSession }
      .andExpect { view { name("move") } }
      .andExpect { status { isOk() } }

    verify(moveService).moveWithPersonJourneysAndEvents(move.moveId, defaultSupplierSerco)
  }

  @Test
  fun `GET move with invalid Move ID for supplier`() {
    val move = moveM1()
    whenever(moveService.moveWithPersonJourneysAndEvents(move.moveId, defaultSupplierSerco)).thenReturn(null)

    mockMvc.get("/moves/${move.moveId}") { session = mockSession }
      .andExpect { status { isNotFound() } }
      .andExpect { view { name("error/404") } }

    verify(moveService).moveWithPersonJourneysAndEvents(move.moveId, defaultSupplierSerco)
  }

  @Test
  internal fun `find a move by valid lowercase reference id with whitespace correctly redirects to move details page`() {

    whenever(moveService.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(Optional.of(moveM1()))

    mockMvc.post("/find-move") {
      session = mockSession
      param("reference", "ref1 ")
    }
      .andExpect { redirectedUrl("/moves/M1") }
      .andExpect { status { is3xxRedirection() } }

    verify(moveService).findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)
  }

  @Test
  internal fun `find a move by a non-existent reference id calls the move service then redirects to search form`() {

    whenever(moveService.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(Optional.of(moveM1()))

    mockMvc.post("/find-move") {
      session = mockSession
      param("reference", "nonexistentref")
    }
      .andExpect { redirectedUrl("/find-move/?no-results-for=nonexistentref") }
      .andExpect { status { is3xxRedirection() } }

    verify(moveService).findMoveByReferenceAndSupplier("NONEXISTENTREF", defaultSupplierSerco)
  }

  @Test
  internal fun `find a move by invalid reference id redirects to search form without calling the move service`() {

    whenever(moveService.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(Optional.of(moveM1()))

    mockMvc.post("/find-move") {
      session = mockSession
      param("reference", "select * from moves")
    }
      .andExpect { redirectedUrl("/find-move/?no-results-for=invalid-reference") }
      .andExpect { status { is3xxRedirection() } }

    verifyNoMoreInteractions(moveService)
  }

  @Test
  internal fun `find a move by move not found redirects to search form without calling the move service`() {

    whenever(moveService.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(Optional.empty())

    mockMvc.post("/find-move") {
      session = mockSession
      param("reference", "REF1")
    }
      .andExpect { redirectedUrl("/find-move/?no-results-for=REF1") }
      .andExpect { status { is3xxRedirection() } }

    verify(moveService).findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)
    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `standard error page is shown when an unexpected exception occurs`() {
    whenever(
      moveService.findMoveByReferenceAndSupplier(
        "REF1",
        defaultSupplierSerco
      )
    ).thenThrow(RuntimeException("Something has gone wrong"))

    mockMvc.post("/find-move") {
      session = mockSession
      param("reference", "REF1")
    }
      .andExpect { view { name("error") } }
      .andExpect { status { is4xxClientError() } }

    verify(monitoringService).capture(any())
  }
}
