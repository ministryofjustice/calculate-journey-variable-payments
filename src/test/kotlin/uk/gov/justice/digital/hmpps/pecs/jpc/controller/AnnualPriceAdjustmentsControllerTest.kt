package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.AnnualPriceAdjustmentsService
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
    whenever(effectiveYear.current()).thenReturn(effectiveYearForDate(effectiveDate))
    whenever(effectiveYear.previous()).thenReturn(effectiveYearForDate(effectiveDate.minusYears(1)))
  }

  @Test
  fun `if not in current effective year or year previous, user can only see the price adjustment history`() {
    whenever(effectiveYear.current()).thenReturn(effectiveDate.year + 2)
    whenever(effectiveYear.previous()).thenReturn(effectiveDate.year + 2)

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
            AnnualPriceAdjustmentsController.AnnualPriceAdjustmentForm("0.0", "0.0")
          )
        }
      }
      .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
      .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
      .andExpect { model { attributeExists("history") } }
      .andExpect { status { isOk() } }

    verify(adjustmentsService).adjustmentsHistoryFor(Supplier.SERCO)
  }

  @Nested
  inner class FailedInflationaryAdjustments {
    @Test
    fun `when invalid format rate`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "1.O5")
        param("details", "some details")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "inflationaryRate", "Pattern") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { model { attributeExists("history") } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }

      verify(adjustmentsService).adjustmentsHistoryFor(Supplier.SERCO)
    }

    @Test
    fun `when zero rate`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "0")
        param("details", "some details")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "inflationaryRate", "rate") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }
    }

    @Test
    fun `when rate with more than 40 decimal places`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "1.99999999999999999999999999999999999999999")
        param("details", "some details")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "inflationaryRate", "Pattern") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }
    }

    @Test
    fun `when rate greater than max rate of 99_9999`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "100.00")
        param("details", "some details")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "inflationaryRate", "Pattern") } }
        .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
        .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
        .andExpect { view { name("annual-price-adjustment") } }
        .andExpect { status { isOk() } }
    }

    @Test
    fun `when missing details for price adjustment`() {
      setOf("", " ").forEach { emptyOrBlankDetails ->
        mockMvc.post("/annual-price-adjustment") {
          session = mockSession
          param("inflationaryRate", "10")
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
    fun `when details too long for price adjustment`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "10")
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
    fun `when details contains invalid characters`() {
      XSS_CHARACTERS.forEach { invalidCharacter ->
        mockMvc.post("/annual-price-adjustment") {
          session = mockSession
          param("inflationaryRate", "10")
          param("volumetricRate", "0.0")
          param("details", invalidCharacter.toString())
        }
          .andExpect { model { attributeHasFieldErrorCode("form", "details", "Invalid details") } }
          .andExpect { model { attribute("contractualYearStart", effectiveYearForDate(effectiveDate).toString()) } }
          .andExpect { model { attribute("contractualYearEnd", (effectiveYearForDate(effectiveDate) + 1).toString()) } }
          .andExpect { view { name("annual-price-adjustment") } }
          .andExpect { status { isOk() } }
      }
    }
  }

  @Nested
  inner class SuccessfulInflationaryAdjustments {
    @Test
    fun `rate and details containing allowed characters are applied for supplier`() {
      setOf(
        "special characters @#$%%^&*()_",
        "special characters `~{}[]:;',./?",
        "inflation rate of 1.123456789012345"
      ).forEach { allowedCharacters ->
        mockMvc.post("/annual-price-adjustment") {
          session = mockSession
          param("inflationaryRate", "1.123456789012345")
          param("volumetricRate", "0.0")
          param("details", allowedCharacters)
        }
          .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
          .andExpect { status { is3xxRedirection() } }

        verify(adjustmentsService).adjust(
          eq(Supplier.SERCO),
          eq(effectiveYearForDate(effectiveDate)),
          eq(AdjustmentMultiplier(1.123456789012345.toBigDecimal())),
          anyOrNull(),
          anyOrNull(),
          eq(allowedCharacters),
          eq(false)
        )

        verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
      }
    }

    @Test
    fun `rate is applied for supplier`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "1.1234")
        param("volumetricRate", "0.0")
        param("details", "some details")
      }
        .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
        .andExpect { status { is3xxRedirection() } }

      verify(adjustmentsService).adjust(
        eq(Supplier.SERCO),
        eq(effectiveYearForDate(effectiveDate)),
        eq(AdjustmentMultiplier("1.1234".toBigDecimal())),
        anyOrNull(),
        anyOrNull(),
        eq("some details"),
        eq(false)
      )

      verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
    }

    @Test
    fun `upto max rate for supplier`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "9.9999999999999999999999999999999999999999")
        param("volumetricRate", "0.0")
        param("details", "some details")
      }
        .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
        .andExpect { status { is3xxRedirection() } }

      verify(adjustmentsService).adjust(
        eq(Supplier.SERCO),
        eq(effectiveYearForDate(effectiveDate)),
        eq(AdjustmentMultiplier("9.9999999999999999999999999999999999999999".toBigDecimal())),
        anyOrNull(),
        anyOrNull(),
        eq("some details"),
        eq(false)
      )

      verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
    }
  }

  @Nested
  inner class SuccessfulInflationaryAndVolumetricAdjustments {
    @Test
    fun `both price increases applied for supplier`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "1.5")
        param("volumetricRate", "2.0")
        param("details", "some details")
      }
        .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
        .andExpect { status { is3xxRedirection() } }

      verify(adjustmentsService).adjust(
        eq(Supplier.SERCO),
        eq(effectiveYearForDate(effectiveDate)),
        eq(AdjustmentMultiplier("1.5".toBigDecimal())),
        eq(AdjustmentMultiplier("2.0".toBigDecimal())),
        anyOrNull(),
        eq("some details"),
        eq(false)
      )

      verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
    }

    @Test
    fun `volumetric price decrease can be applied for supplier`() {
      mockMvc.post("/annual-price-adjustment") {
        session = mockSession
        param("inflationaryRate", "1.5")
        param("volumetricRate", "-2.0")
        param("details", "some details")
      }
        .andExpect { redirectedUrl("/manage-journey-price-catalogue") }
        .andExpect { status { is3xxRedirection() } }

      verify(adjustmentsService).adjust(
        eq(Supplier.SERCO),
        eq(effectiveYearForDate(effectiveDate)),
        eq(AdjustmentMultiplier("1.5".toBigDecimal())),
        eq(AdjustmentMultiplier("-2.0".toBigDecimal())),
        anyOrNull(),
        eq("some details"),
        eq(false)
      )

      verify(adjustmentsService, never()).adjustmentsHistoryFor(any())
    }
  }
}
