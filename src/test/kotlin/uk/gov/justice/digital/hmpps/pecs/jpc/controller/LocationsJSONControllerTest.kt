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
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultSupplierSerco
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class LocationsJSONControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  private val mockSession = MockHttpSession(wac.servletContext)

  @MockBean
  lateinit var locationService: MapFriendlyLocationService

  @BeforeEach
  fun beforeEach() {
    mockSession.setAttribute("supplier", defaultSupplierSerco)
  }

  @Test
  internal fun `locations_json with old version number returns locations and version number`() {
    whenever(locationService.getVersion()).thenReturn(12345)
    whenever(locationService.findAll()).thenReturn(listOf(Location(LocationType.PR, "TESTLOC1", "Test Loc 1")))

    mockMvc.get("/locations.json") {
      session = mockSession
      param("locationsVersion", "-1")
    }
      .andExpect { content { json("{version:12345,locations:{TESTLOC1:\"Test Loc 1\"}}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `locations_json with newest version number returns just version number`() {
    whenever(locationService.getVersion()).thenReturn(12345)

    mockMvc.get("/locations.json") {
      session = mockSession
      param("locationsVersion", "12345")
    }
      .andExpect { content { json("{version:12345}") } }
      .andExpect { status { is2xxSuccessful() } }
  }
}
