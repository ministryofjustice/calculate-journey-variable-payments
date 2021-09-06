package uk.gov.justice.digital.hmpps.pecs.jpc.controller

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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.defaultSupplierSerco

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ApplicationInformationControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  private val mockSession = MockHttpSession(wac.servletContext)

  @MockBean
  lateinit var annualPriceAdjustmentsService: AnnualPriceAdjustmentsService

  @BeforeEach
  fun beforeEach() {
    mockSession.setAttribute("supplier", defaultSupplierSerco)
  }

  @Test
  internal fun `application info message is present when a price adjustment is in progress`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(true)

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{message:\"A bulk price adjustment is currently in progress. Any further price changes will be prevented until the adjustment is complete.\"}}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `application info message is not present when a price adjustment is not in progress`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{}") } }
      .andExpect { status { is2xxSuccessful() } }
  }
}
