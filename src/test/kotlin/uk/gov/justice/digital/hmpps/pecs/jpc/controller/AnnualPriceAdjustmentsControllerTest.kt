package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
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

  @Test
  fun `user with the maintain price role can navigate to the page`() {
    mockMvc.get("/annual-price-adjustment") { session = mockSession }
      .andExpect {
        model {
          attribute(
            "form",
            AnnualPriceAdjustmentsController.AnnualPriceAdjustmentForm("0.000")
          )
        }
      }
      .andExpect { status { isOk() } }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `user without the maintain price role cannot navigate to the page`() {
    mockMvc.get("/annual-price-adjustment") { session = mockSession }.andExpect { status { isForbidden() } }
  }

  @Test
  fun `parsing of adjustment rate`() {
    assertThat(parseAdjustment("")).isNull()
    assertThat(parseAdjustment(" ")).isNull()
    assertThat(parseAdjustment("O.OO")).isNull()
    assertThat(parseAdjustment("O.OO")).isNull()
    assertThat(parseAdjustment("1.2345")).isNull()
    assertThat(parseAdjustment("12345.6789")).isNull()
    assertThat(parseAdjustment("1.234")).isEqualTo(1.234)
    assertThat(parseAdjustment("12345.678")).isEqualTo(12345.678)
  }
}
