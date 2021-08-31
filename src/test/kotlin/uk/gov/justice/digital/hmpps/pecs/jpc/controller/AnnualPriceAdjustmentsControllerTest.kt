package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@WithMockUser(roles = ["PECS_MAINTAIN_PRICE"])
class AnnualPriceAdjustmentsControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val effectiveDate = LocalDate.now()

  private val mockSession = MockHttpSession(wac.servletContext).apply {
    this.setAttribute("supplier", Supplier.SERCO)
    this.setAttribute("date", effectiveDate)
  }

  @MockBean
  private lateinit var adjustmentsService: AnnualPriceAdjustmentsService

  @Test
  fun `user with the maintain price role can navigate to the page`() {
    mockMvc.get("/annual-price-adjustment") { session = mockSession }
      .andExpect {
        model {
          attribute(
            "form",
            AnnualPriceAdjustmentsController.AnnualPriceAdjustmentForm("0.0000")
          )
        }
      }
      .andExpect { status { isOk() } }

    verify(adjustmentsService).adjustmentsHistoryFor(Supplier.SERCO)
  }

  @Test
  fun `fails upon submission of an invalid format rate price adjustment`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "1.O5")
      param("details", "some details")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "rate", "Pattern") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }

    verify(adjustmentsService).adjustmentsHistoryFor(Supplier.SERCO)
  }

  @Test
  fun `fails upon submission of an zero rate price adjustment`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "0")
      param("details", "some details")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "rate", "rate") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `fails upon submission of an rate with more than 4 decimal places `() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "1.12345")
      param("details", "some details")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "rate", "Pattern") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `fails upon submission when missing details for price adjustment`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "10")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "details", "NotEmpty") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `fails upon submission when details too long for price adjustment`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "10")
      param("details", "z".padEnd(256, 'z'))
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "details", "Length") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `user without the maintain price role cannot navigate to the page`() {
    mockMvc.get("/annual-price-adjustment") { session = mockSession }.andExpect { status { isForbidden() } }
  }

  @Test
  fun `annual price adjustment with valid rate is applied for supplier`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "1.1234")
      param("details", "some details")
    }
      .andExpect { view { name("manage-journey-price-catalogue") } }
      .andExpect { status { isOk() } }

    verify(adjustmentsService).adjust(eq(Supplier.SERCO), eq(effectiveYearForDate(effectiveDate)), eq(1.1234), anyOrNull(), eq("some details"))
    verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
  }
}
