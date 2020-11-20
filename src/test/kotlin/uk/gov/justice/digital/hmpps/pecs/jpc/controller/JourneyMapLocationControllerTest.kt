package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.whenever
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyMapLocationService

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class JourneyMapLocationControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  private val journeyId = "123456"

  @MockBean
  lateinit var service: JourneyMapLocationService

  @Test
  @WithMockUser(username = "Anonymous", roles = ["PECS_JPC"])
  internal fun `mapping of pickup location for serco`() {
    mockSession.apply { this.setAttribute("supplier", "SERCO") }

    whenever(service.findPickUpAndDropOffAgenciesForJourney(journeyId)).thenReturn(Pair("1", "2"))

    mockMvc.get("/map-pickup-location/${journeyId}") { session = mockSession }
            .andExpect { model { attribute("supplier", "SERCO") } }
            .andExpect { model { attribute("form", JourneyMapLocationController.MapLocationForm("1")) } }
            .andExpect { view { name("map-location") } }
            .andExpect { status { isOk } }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  internal fun `mapping of drop off location for geoamey`() {
    mockSession.apply { this.setAttribute("supplier", "GEOAMEY") }

    whenever(service.findPickUpAndDropOffAgenciesForJourney(journeyId)).thenReturn(Pair("3", "4"))

    mockMvc.get("/map-drop-off-location/${journeyId}") { session = mockSession }
            .andExpect { model { attribute("supplier", "GEOAMEY") } }
            .andExpect { model { attribute("form", JourneyMapLocationController.MapLocationForm("4")) } }
            .andExpect { view { name("map-location") } }
            .andExpect { status { isOk } }
  }
}
