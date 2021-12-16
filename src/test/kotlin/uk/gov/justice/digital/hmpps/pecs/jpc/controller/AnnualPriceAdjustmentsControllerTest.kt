package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
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

  @MockBean
  private lateinit var effectiveYear: EffectiveYear

  private val mockSession = MockHttpSession(wac.servletContext).apply {
    this.setAttribute("supplier", Supplier.SERCO)
    this.setAttribute("date", effectiveDate)
  }

  @MockBean
  private lateinit var adjustmentsService: AnnualPriceAdjustmentsService

  @BeforeEach
  fun before() {
    whenever(effectiveYear.current()).thenReturn(effectiveDate.year)
  }

  @Test
  fun `user can only see the price adjustment history if not in current effective year`() {
    whenever(effectiveYear.current()).thenReturn(effectiveDate.year + 1)

    mockMvc.get("/annual-price-adjustment") { session = mockSession }
      .andExpect { view { name("annual-price-adjustment-history") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { model { attributeExists("history") } }
      // including check for the feedback URL
      .andExpect { model { attribute("feedbackUrl", "#") } }
      // including check for the navigation
      .andExpect { model { attribute("navigation", "PRICE") } }
      .andExpect { status { isOk() } }

    verify(adjustmentsService).adjustmentsHistoryFor(Supplier.SERCO)
  }

  @Test
  fun `user with the maintain price role can navigate to the page`() {
    mockMvc.get("/annual-price-adjustment") { session = mockSession }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect {
        model {
          attribute(
            "form",
            AnnualPriceAdjustmentsController.AnnualPriceAdjustmentForm("0.0")
          )
        }
      }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { model { attributeExists("history") } }
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
      .andExpect { model { attributeExists("history") } }
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
  fun `fails upon submission of an rate with more than 15 decimal places`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "1.12345678901234501")
      param("details", "some details")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "rate", "Pattern") } }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { view { name("annual-price-adjustment") } }
      .andExpect { status { isOk() } }
  }

  @Test
  fun `fails upon submission of an rate greater than max rate of 9_9999`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "10.00")
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
    setOf("", " ").forEach { emptyOrBlankDetails ->
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("rate", "10")
        param("details", emptyOrBlankDetails)
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "details", "NotBlank") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }
    }
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
  fun `fails upon submission when details contains potential cross site scripting characters`() {
    XSS_CHARACTERS.forEach { invalidCharacter ->
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("rate", "10")
        param("details", invalidCharacter.toString())
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "details", "Invalid details") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }
    }
  }

  @Test
  fun `annual price adjustment with valid rate and details containing allowed characters are applied for supplier`() {
    setOf("special characters @#$%%^&*()_", "special characters `~{}[]:;',./?", "inflation rate of 1.123456789012345").forEach { allowedCharacters ->
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("rate", "1.123456789012345")
        param("details", allowedCharacters)
      }
        .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
        .andExpect { status { is3xxRedirection() } }

      verify(adjustmentsService).adjust(
        eq(Supplier.SERCO),
        eq(effectiveYearForDate(effectiveDate)),
        eq(AdjustmentMultiplier(1.123456789012345.toBigDecimal())),
        anyOrNull(),
        eq(allowedCharacters)
      )
      verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
    }
  }

  @Test
  fun `annual price adjustment with valid rate is applied for supplier`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "1.1234")
      param("details", "some details")
    }
      .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
      .andExpect { status { is3xxRedirection() } }

    verify(adjustmentsService).adjust(
      eq(Supplier.SERCO),
      eq(effectiveYearForDate(effectiveDate)),
      eq(AdjustmentMultiplier("1.1234".toBigDecimal())),
      anyOrNull(),
      eq("some details")
    )
    verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
  }

  @Test
  fun `annual price adjustment succeeds upto max rate for supplier`() {
    mockMvc.post("/annual-price-adjustment") {
      session = mockSession
      param("rate", "9.9999")
      param("details", "some details")
    }
      .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
      .andExpect { status { is3xxRedirection() } }

    verify(adjustmentsService).adjust(
      eq(Supplier.SERCO),
      eq(effectiveYearForDate(effectiveDate)),
      eq(AdjustmentMultiplier(9.9999.toBigDecimal())),
      anyOrNull(),
      eq("some details")
    )
    verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
  }
}
