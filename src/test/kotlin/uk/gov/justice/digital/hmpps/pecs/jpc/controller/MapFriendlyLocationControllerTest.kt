package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.LocationsService
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class MapFriendlyLocationControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  private val agencyId = "123456"

  private val nomisLocationName = "NOMIS Location Name"

  private val maxAllowedCharacters = 255

  @MockBean
  lateinit var service: LocationsService

  @MockBean
  lateinit var basmClientApiService: BasmClientApiService

  @Test
  internal fun `get mapping for new friendly location`() {
    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(null)
    whenever(basmClientApiService.findNomisAgencyLocationNameBy(agencyId)).thenReturn(nomisLocationName)

    mockMvc.get("/map-location/$agencyId")
      .andExpect {
        model {
          attribute(
            "form",
            MapFriendlyLocationController.MapLocationForm(agencyId, nomisLocationName = nomisLocationName)
          )
        }
      }
      // including check for the navigation
      .andExpect { model { attribute("navigation", "LOCATION") } }
      .andExpect { view { name("add-location") } }
      .andExpect { status { isOk() } }

    verify(service).findAgencyLocationAndType(agencyId)
  }

  @Test
  internal fun `get mapping for new friendly location with no NOMIS location name lookup match`() {
    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(null)
    whenever(basmClientApiService.findNomisAgencyLocationNameBy(agencyId)).thenReturn(null)

    mockMvc.get("/map-location/$agencyId")
      .andExpect {
        model {
          attribute(
            "form",
            MapFriendlyLocationController.MapLocationForm(
              agencyId,
              nomisLocationName = "Sorry, we are currently unable to retrieve the NOMIS Location Name. Please try again later."
            )
          )
        }
      }
      .andExpect { view { name("add-location") } }
      .andExpect { status { isOk() } }

    verify(service).findAgencyLocationAndType(agencyId)
  }

  @Test
  internal fun `get mapping for existing friendly location with location history`() {
    val (agencyId, agencyName, agencyType) = Triple("ABCDEF", "EXISTING LOCATION", LocationType.CC)
    val auditEventDatetime = LocalDateTime.now()
    val auditEventMetadata = MapLocationMetadata(agencyId, newName = agencyName, newType = agencyType)

    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(Triple(agencyId, agencyName, agencyType))

    whenever(service.locationHistoryForAgencyId(agencyId)).thenReturn(
      setOf(
        AuditEvent(
          AuditEventType.LOCATION,
          auditEventDatetime,
          "Jane",
          auditEventMetadata
        )
      )
    )

    whenever(basmClientApiService.findNomisAgencyLocationNameBy(agencyId)).thenReturn(nomisLocationName)

    mockMvc.get("/map-location/$agencyId?origin=from")
      .andExpect {
        model {
          attribute(
            "form",
            MapFriendlyLocationController.MapLocationForm(
              "ABCDEF",
              "EXISTING LOCATION",
              LocationType.CC,
              "update",
              nomisLocationName
            )
          )
        }
      }
      .andExpect {
        model {
          attribute(
            "history",
            listOf(
              LocationHistoryDto(
                auditEventDatetime,
                "Assigned to location name 'EXISTING LOCATION' and type 'Crown Court'",
                "Jane"
              )
            )
          )
        }
      }
      .andExpect { view { name("update-location") } }
      .andExpect { model { attribute("origin", "from") } }
      .andExpect { status { isOk() } }

    verify(service).findAgencyLocationAndType(agencyId)
  }

  @Test
  internal fun `map existing location successful when originating search based on pick-up`() {
    whenever(service.locationAlreadyExists(agencyId, "Updated Friendly Location Name")).thenReturn(false)

    mockSession.apply {
      this.setAttribute("origin", "from")
      this.setAttribute("pick-up", "FRIENDLY LOCATION NAME")
    }

    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("nomisLocationName", nomisLocationName)
      param("locationName", "Updated Friendly Location Name")
      param("locationType", "CC")
      param("operation", "update")
      session = mockSession
    }
      .andExpect { flash { attribute("flashAttrMappedLocationName", "UPDATED FRIENDLY LOCATION NAME") } }
      .andExpect { flash { attribute("flashAttrMappedAgencyId", "123456") } }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { redirectedUrl("/journeys-results?pick-up=UPDATED%20FRIENDLY%20LOCATION%20NAME") }

    verify(service).locationAlreadyExists(agencyId, "Updated Friendly Location Name")
    verify(service).setLocationDetails(agencyId, "Updated Friendly Location Name", LocationType.CC)
  }

  @Test
  internal fun `map existing location successful when originating search based on drop-off`() {
    whenever(service.locationAlreadyExists(agencyId, "Updated Friendly Location Name")).thenReturn(false)

    mockSession.apply {
      this.setAttribute("origin", "to")
      this.setAttribute("drop-off", "FRIENDLY LOCATION NAME")
    }

    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("nomisLocationName", nomisLocationName)
      param("locationName", "Updated Friendly Location Name")
      param("locationType", "CC")
      param("operation", "update")
      session = mockSession
    }
      .andExpect { flash { attribute("flashAttrMappedLocationName", "UPDATED FRIENDLY LOCATION NAME") } }
      .andExpect { flash { attribute("flashAttrMappedAgencyId", "123456") } }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { redirectedUrl("/journeys-results?drop-off=UPDATED%20FRIENDLY%20LOCATION%20NAME") }

    verify(service).locationAlreadyExists(agencyId, "Updated Friendly Location Name")
    verify(service).setLocationDetails(agencyId, "Updated Friendly Location Name", LocationType.CC)
  }

  @Test
  internal fun `map existing location successful when originating search based on pick-up and drop-off`() {
    whenever(service.locationAlreadyExists(agencyId, "Updated pick-up location")).thenReturn(false)

    mockSession.apply {
      this.setAttribute("origin", "from")
      this.setAttribute("pick-up", "PICK-UP LOCATION")
      this.setAttribute("drop-off", "DROP-OFF LOCATION")
    }

    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("nomisLocationName", nomisLocationName)
      param("locationName", "Updated pick-up location")
      param("locationType", "CC")
      param("operation", "update")
      session = mockSession
    }
      .andExpect { flash { attribute("flashAttrMappedLocationName", "UPDATED PICK-UP LOCATION") } }
      .andExpect { flash { attribute("flashAttrMappedAgencyId", "123456") } }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { redirectedUrl("/journeys-results?pick-up=UPDATED%20PICK-UP%20LOCATION&drop-off=DROP-OFF%20LOCATION") }

    verify(service).locationAlreadyExists(agencyId, "Updated pick-up location")
    verify(service).setLocationDetails(agencyId, "Updated pick-up location", LocationType.CC)
  }

  @Test
  internal fun `map new location successful when mandatory criteria supplied`() {
    val expectedLocationName = "Friendly Location Name".padEnd(maxAllowedCharacters, 'z')

    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("nomisLocationName", nomisLocationName)
      param("locationName", expectedLocationName)
      param("locationType", "CC")
    }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { flash { attribute("flashAttrMappedLocationName", expectedLocationName.uppercase()) } }
      .andExpect { flash { attribute("flashAttrMappedAgencyId", "123456") } }
      .andExpect { redirectedUrl("/journeys") }

    verify(service).locationAlreadyExists(agencyId, expectedLocationName)
    verify(service).setLocationDetails(agencyId, expectedLocationName, LocationType.CC)
  }

  @Test
  internal fun `map new location fails when mandatory criteria not supplied`() {
    setOf("", " ").forEach { emptyOrBlankLocation ->
      mockMvc.post("/map-location") {
        param("agencyId", "123456")
        param("nomisLocationName", nomisLocationName)
        param("locationName", emptyOrBlankLocation)
        param("locationType", "CC")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "NotBlank") } }
        .andExpect { view { name("add-location") } }
        .andExpect { status { isOk() } }

      verify(service, never()).locationAlreadyExists(any(), any())
      verify(service, never()).setLocationDetails(any(), any(), any())
    }
  }

  @Test
  internal fun `map new location fails when location name too long`() {
    mockMvc.post("/map-location") {
      param("agencyId", "123456")
      param("nomisLocationName", nomisLocationName)
      param("locationName", "z".padEnd(maxAllowedCharacters + 1, 'z'))
      param("locationType", "CC")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "Length") } }
      .andExpect { view { name("add-location") } }
      .andExpect { status { isOk() } }

    verify(service, never()).setLocationDetails(any(), any(), any())
  }

  @Test
  internal fun `map new location fails when duplicate location supplied supplied`() {
    whenever(service.locationAlreadyExists(agencyId, "Duplicate location")).thenReturn(true)

    mockMvc.post("/map-location") {
      param("agencyId", agencyId)
      param("nomisLocationName", nomisLocationName)
      param("locationName", "Duplicate location")
      param("locationType", "CC")
    }
      .andExpect { view { name("add-location") } }
      .andExpect { status { isOk() } }

    verify(service).locationAlreadyExists(agencyId, "Duplicate location")
    verify(service, never()).setLocationDetails(any(), any(), any())
  }

  @Test
  fun `fails upon submission when location name contains potential cross site scripting characters`() {
    XSS_CHARACTERS.forEach { invalidCharacter ->
      mockMvc.post("/map-location") {
        param("agencyId", "123456")
        param("nomisLocationName", nomisLocationName)
        param("locationName", invalidCharacter.toString())
        param("locationType", "CC")
      }
        .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "Invalid location") } }
        .andExpect { view { name("add-location") } }
        .andExpect { status { isOk() } }

      verify(service, never()).setLocationDetails(any(), any(), any())
    }
  }
}
