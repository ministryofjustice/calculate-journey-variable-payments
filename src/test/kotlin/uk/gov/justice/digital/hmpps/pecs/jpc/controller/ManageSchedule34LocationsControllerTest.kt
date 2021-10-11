package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.LocationsService
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ManageSchedule34LocationsControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val maxAllowedCharacters = 255

  @MockBean
  lateinit var service: LocationsService

  @MockBean
  lateinit var basmClientApiService: BasmClientApiService

  @Test
  internal fun `get and display search locations screen`() {
    mockMvc.get("/search-locations")
      .andExpect { model { attribute("form", ManageSchedule34LocationsController.SearchLocationForm()) } }
      .andExpect { view { name("search-locations") } }
      .andExpect { status { isOk() } }
      // including check for the navigation
      .andExpect { model { attribute("navigation", "LOCATION") } }

    verifyZeroInteractions(service)
  }

  @Test
  internal fun `search for existing location redirects to manage location screen`() {
    whenever(service.findLocationBySiteName("LOCATION NAME")).thenReturn(
      Location(
        LocationType.PR,
        "AGENCY_ID",
        "LOCATION NAME"
      )
    )

    mockMvc.post("/search-locations") {
      param("location", "LOCATION NAME")
    }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { redirectedUrl("/manage-location/AGENCY_ID") }

    verify(service).findLocationBySiteName("LOCATION NAME")
  }

  @Test
  internal fun `get manage location screen and location audit history`() {
    val (agencyId, agencyName, agencyType) = Triple("ABCDEF", "AGENCY NAME", LocationType.CC)
    val auditEventDatetime = LocalDateTime.now()
    val auditEventMetadata = MapLocationMetadata(agencyId, newName = agencyName, newType = agencyType)

    whenever(service.findAgencyLocationAndType(agencyId)).thenReturn(Triple(agencyId, agencyName, agencyType))
    whenever(basmClientApiService.findNomisAgencyLocationNameBy(agencyId)).thenReturn("DIFFERENT AGENCY NAME")
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

    mockMvc.get("/manage-location/ABCDEF")
      .andExpect {
        model {
          attribute(
            "form",
            ManageSchedule34LocationsController.LocationForm(
              agencyId,
              agencyName,
              LocationType.CC,
              "DIFFERENT AGENCY NAME"
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
                "Assigned to location name 'AGENCY NAME' and type 'Crown Court'",
                "Jane"
              )
            )
          )
        }
      }
      .andExpect { view { name("manage-location") } }
      .andExpect { status { isOk() } }

    verify(service).locationHistoryForAgencyId(agencyId)
  }

  @Test
  internal fun `perform location change succeeds`() {
    val expectedLocationName = "Friendly Location Name".padEnd(maxAllowedCharacters, 'z')

    mockMvc.post("/manage-location") {
      param("agencyId", "AGENCY_ID")
      param("locationName", expectedLocationName)
      param("locationType", "PR")
    }
      .andExpect { status { is3xxRedirection() } }
      .andExpect { redirectedUrl("search-locations") }

    verify(service).locationAlreadyExists("AGENCY_ID", expectedLocationName)
    verify(service).setLocationDetails("AGENCY_ID", expectedLocationName, LocationType.PR)
  }

  @Test
  internal fun `perform location change fails when location name too long`() {
    mockMvc.post("/manage-location") {
      param("agencyId", "AGENCY_ID")
      param("locationName", "z".padEnd(maxAllowedCharacters + 1, 'z'))
      param("locationType", "PR")
    }
      .andExpect { status { is2xxSuccessful() } }
      .andExpect { view { name("manage-location") } }
      .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "Length") } }

    verify(service, never()).setLocationDetails(any(), any(), any())
  }

  @Test
  internal fun `perform location change fails on duplicate name`() {
    whenever(service.locationAlreadyExists("AGENCY_ID", "DUPLICATE LOCATION NAME")).thenReturn(true)

    mockMvc.post("/manage-location") {
      param("agencyId", "AGENCY_ID")
      param("locationName", "DUPLICATE LOCATION NAME")
      param("locationType", "PR")
    }
      .andExpect { status { is2xxSuccessful() } }
      .andExpect { view { name("manage-location") } }
      .andExpect { model { attributeHasFieldErrorCode("form", "locationName", "duplicate") } }

    verify(service).locationAlreadyExists("AGENCY_ID", "DUPLICATE LOCATION NAME")
    verify(service, never()).setLocationDetails(any(), any(), any())
  }

  @Test
  internal fun `search for location location not found returns back to search screen`() {
    whenever(service.findLocationBySiteName(any())).thenReturn(null)

    mockMvc.post("/search-locations") {
      param("location", "LOCATION NAME")
    }
      .andExpect { status { is2xxSuccessful() } }
      .andExpect { view { name("search-locations") } }
      .andExpect { model { attributeHasFieldErrorCode("form", "location", "notfound") } }
  }

  @Test
  internal fun `search for empty location returns back to search screen`() {
    mockMvc.post("/search-locations") {
      param("location", "")
    }
      .andExpect { status { is2xxSuccessful() } }
      .andExpect { view { name("search-locations") } }

    verifyZeroInteractions(service)
  }

  @Test
  internal fun `search for blank location returns back to search screen`() {
    mockMvc.post("/search-locations") {
      param("location", " ")
    }
      .andExpect { status { is2xxSuccessful() } }
      .andExpect { view { name("search-locations") } }

    verifyZeroInteractions(service)
  }
}
