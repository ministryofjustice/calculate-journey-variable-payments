package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.LocationsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.defaultSupplierSerco

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ReferenceDataControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  private val mockSession = MockHttpSession(wac.servletContext)

  @MockitoBean
  lateinit var locationService: LocationsService

  @BeforeEach
  fun beforeEach() {
    mockSession.setAttribute("supplier", defaultSupplierSerco)
  }

  @Test
  internal fun `get with old version number returns locations and version number`() {
    whenever(locationService.getVersion()).thenReturn(12345)
    whenever(locationService.findAll()).thenReturn(listOf(Location(LocationType.PR, "TESTLOC1", "Test Loc 1")))

    mockMvc.get("/reference/locations") {
      session = mockSession
      param("version", "-1")
    }
      .andExpect { content { json("{version:12345,locations:{TESTLOC1:\"Test Loc 1\"}}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `get with old version number returns multiple locations and version number`() {
    whenever(locationService.getVersion()).thenReturn(12345)
    whenever(locationService.findAll()).thenReturn(
      listOf(
        Location(LocationType.PR, "TESTLOC1", "Test Loc 1"),
        Location(LocationType.PR, "TESTLOC2", "Test Loc 2"),
        Location(LocationType.PR, "TESTLOC3", "Test Loc 3"),
      ),
    )

    mockMvc.get("/reference/locations") {
      session = mockSession
      param("version", "-1")
    }
      .andExpect { content { json("{version:12345,locations:{TESTLOC1:\"Test Loc 1\",TESTLOC2:\"Test Loc 2\",TESTLOC3:\"Test Loc 3\"}}") } }
      .andExpect { status { is2xxSuccessful() } }
  }

  @Test
  internal fun `get with newest version number returns just version number`() {
    whenever(locationService.getVersion()).thenReturn(12345)

    mockMvc.get("/reference/locations") {
      session = mockSession
      param("version", "12345")
    }
      .andExpect { content { json("{version:12345}") } }
      .andExpect { status { is2xxSuccessful() } }
  }
}
