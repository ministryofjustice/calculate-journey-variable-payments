package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class MapFriendlyLocationControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val agencyId = "123456"

  @MockBean
  lateinit var service: MapFriendlyLocationService

  @Test
  internal fun `get mapping for new friendly location`() {
    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(null)

    mockMvc.get("/map-location/${agencyId}")
            .andExpect { model { attribute("form", MapFriendlyLocationController.MapLocationForm(agencyId)) } }
            .andExpect { model { attribute("operation", "create" ) } }
            .andExpect { view { name("map-location") } }
            .andExpect { status { isOk } }

    verify(service).findAgencyLocationAndType(agencyId)
  }

  @Test
  internal fun `get mapping for existing friendly location`() {
    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(Triple(agencyId, "existing location", LocationType.AP))

    mockMvc.get("/map-location/${agencyId}")
            .andExpect { model { attribute("form", MapFriendlyLocationController.MapLocationForm(agencyId, "existing location", LocationType.AP.name)) } }
            .andExpect { model { attribute("operation", "update" ) } }
            .andExpect { view { name("map-location") } }
            .andExpect { status { isOk } }

    verify(service).findAgencyLocationAndType(agencyId)
  }

  @Test
  internal fun `map new location successful when mandatory criteria supplied`() {
    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("locationName", "Friendly Location Name")
      param("locationType", "CC")
    }
            .andExpect { status { is3xxRedirection } }
            .andExpect { flash { attribute("flashAttrMappedLocationName", "Friendly Location Name") } }
            .andExpect { flash { attribute("flashAttrMappedAgencyId", "123456") } }
            .andExpect { redirectedUrl("/journeys") }

    verify(service).locationAlreadyExists(agencyId, "Friendly Location Name")
    verify(service).mapFriendlyLocation(agencyId, "Friendly Location Name", LocationType.CC)
  }

  @Test
  internal fun `map new location fails when mandatory criteria not supplied`() {
    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("locationName", "")
      param("locationType", "CC")
    }
            .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "NotEmpty") } }
            .andExpect { view { name("/map-location") } }
            .andExpect { status { isOk } }

    verify(service, never()).locationAlreadyExists(any(), any())
    verify(service, never()).mapFriendlyLocation(any(), any(), any())
  }

  @Test
  internal fun `map new location fails when duplicate location supplied supplied`() {
    whenever(service.locationAlreadyExists(agencyId, "Duplicate location")).thenReturn(true)

    mockMvc.post("/map-location") {
      param("agencyId", agencyId)
      param("locationName", "Duplicate location")
      param("locationType", "CC")
    }
            .andExpect { model { attributeErrorCount("form", 1) } }
            .andExpect { view { name("/map-location") } }
            .andExpect { status { isOk } }

    verify(service).locationAlreadyExists(agencyId, "Duplicate location")
    verify(service, never()).mapFriendlyLocation(any(), any(), any())
  }
}
