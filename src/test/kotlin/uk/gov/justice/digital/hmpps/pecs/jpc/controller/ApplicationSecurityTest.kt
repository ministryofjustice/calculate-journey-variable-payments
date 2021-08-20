package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestPropertySource(properties = ["HMPPS_AUTH_BASE_URI=http://fake_auth_redirect_url"])
internal class ApplicationSecurityTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders
    .webAppContextSetup(wac)
    .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
    .build()

  private val mockSession = MockHttpSession(wac.servletContext)

  @Test
  @WithMockUser(username = "Anonymous", roles = ["PECS_JPC"])
  fun `logout from the dashboard results in unauthenticated`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
      this.setAttribute("date", LocalDate.now().withDayOfMonth(1))
    }

    mockMvc.get("/dashboard") { session = mockSession }
      .andExpect { status { isOk() } }
      .andExpect {
        authenticated()
          .withAuthenticationName("Anonymous")
          .withRoles("PECS_JPC")
      }
      .andDo { mockMvc.perform(logout()) }
      .andExpect { unauthenticated() }
      .andExpect { status { isOk() } }
      .andReturn()
  }

  @Test
  @WithMockUser(username = "Unauthorised_User")
  fun `default session is invalidated on unauthorised access`() {
    assertThat(mockSession.isInvalid).isFalse

    mockMvc.get("/") { session = mockSession }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { unauthenticated() }
      .andExpect { request { redirectedUrl("http://fake_auth_redirect_url") } }

    assertThat(mockSession.isInvalid).isTrue
  }
}
