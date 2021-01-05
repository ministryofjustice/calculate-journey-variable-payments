package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class LogoutApplicationTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  @Test
  @WithMockUser(username = "Anonymous", roles = ["PECS_JPC"])
  fun `logout from the dashboard results in unauthenticated`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
      this.setAttribute("date", LocalDate.now().withDayOfMonth(1))
    }

    mockMvc.get("/dashboard") { session = mockSession }
      .andExpect { status { isOk } }
      .andExpect {
        authenticated()
          .withAuthenticationName("Anonymous")
          .withRoles("PECS_JPC")
      }
      .andDo { mockMvc.post("/logout") { session = mockSession } }
      .andExpect { unauthenticated() }
      .andExpect { status { isOk } }
      .andReturn()
  }
}
