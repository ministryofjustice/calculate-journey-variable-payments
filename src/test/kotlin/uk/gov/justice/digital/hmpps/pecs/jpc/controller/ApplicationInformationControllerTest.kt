package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.defaultSupplierSerco
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ApplicationInformationControllerTest(
  @Autowired private val wac: WebApplicationContext,
  @Autowired private val timesource: TimeSource,
) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val effectiveDate = LocalDate.of(2021, 9, 1)

  private val mockSession = MockHttpSession(wac.servletContext).apply {
    this.setAttribute("supplier", Supplier.SERCO)
    this.setAttribute("date", effectiveDate)
  }

  @MockBean
  lateinit var annualPriceAdjustmentsService: AnnualPriceAdjustmentsService

  @MockBean
  lateinit var importReportsService: ImportReportsService

  @MockBean
  lateinit var effectiveYear: EffectiveYear

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

  @Test
  internal fun `application info message is not present when in allowed price change window`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)
    whenever(effectiveYear.canAddOrUpdatePrices(any())).thenReturn(true)

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `application info message is present when outside allowed price change window`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)
    whenever(effectiveYear.canAddOrUpdatePrices(any())).thenReturn(false)

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{message:\"Prices for the selected catalogue year September 2021 to August 2022 can no longer be changed.\"}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `application info message is present when reports feed is behind by 2 days`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)
    whenever(effectiveYear.canAddOrUpdatePrices(any())).thenReturn(true)
    whenever(importReportsService.dateOfLastImport()).thenReturn(timesource.date().minusDays(2))

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{message:\"The service may be missing pricing data, please contact the Book a secure move team.\"}}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `application info message is not present when reports feed is behind by 1 day`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)
    whenever(effectiveYear.canAddOrUpdatePrices(any())).thenReturn(true)
    whenever(importReportsService.dateOfLastImport()).thenReturn(timesource.date().minusDays(1))

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `application info message is not present when no existing feed`() {
    whenever(annualPriceAdjustmentsService.adjustmentInProgressFor(defaultSupplierSerco)).thenReturn(false)
    whenever(effectiveYear.canAddOrUpdatePrices(any())).thenReturn(true)
    whenever(importReportsService.dateOfLastImport()).thenReturn(null)

    mockMvc.get("/app/info") {
      session = mockSession
    }
      .andExpect { content { json("{}") } }
      .andExpect { status { is2xxSuccessful() } }
  }
}
