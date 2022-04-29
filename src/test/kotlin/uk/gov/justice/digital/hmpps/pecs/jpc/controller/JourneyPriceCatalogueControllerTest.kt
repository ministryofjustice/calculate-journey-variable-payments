package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.FakeAuthentication
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@ExtendWith(FakeAuthentication::class)
class JourneyPriceCatalogueControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  @MockBean
  private lateinit var timeSource: TimeSource

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `can generate spreadsheet with correct name for Serco`() {
    whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
    whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

    mockSession.addSupplierAndDateAttributes(Supplier.SERCO, LocalDate.of(2020, 10, 1))

    mockMvc.get("/generate-prices-spreadsheet") {
      session = mockSession
    }
      .andExpect {
        status { isOk() }
        content { contentType("application/vnd.ms-excel") }
        header {
          string(
            "Content-Disposition",
            "attachment;filename=Journey_Variable_Payment_Output_SERCO_2020-10-13_15_25.xlsx"
          )
        }
      }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `can generate spreadsheet with correct name for Geoamey`() {
    whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
    whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

    mockSession.addSupplierAndDateAttributes(Supplier.GEOAMEY, LocalDate.of(2020, 10, 1))

    mockMvc.get("/generate-prices-spreadsheet") {
      session = mockSession
    }
      .andExpect {
        status { isOk() }
        content { contentType("application/vnd.ms-excel") }
        header {
          string(
            "Content-Disposition",
            "attachment;filename=Journey_Variable_Payment_Output_GEOAMEY_2020-10-13_15_25.xlsx"
          )
        }
      }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `no content when supplier not present on the session`() {
    mockSession.addSupplierAndDateAttributes(null, LocalDate.of(2020, 10, 1))

    mockMvc.get("/generate-prices-spreadsheet") {
      session = mockSession
    }
      .andExpect {
        status { isNoContent() }
      }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `no content when date not present on the session`() {
    mockSession.addSupplierAndDateAttributes(Supplier.GEOAMEY, null)

    mockMvc.get("/generate-prices-spreadsheet") {
      session = mockSession
    }
      .andExpect {
        status { isNoContent() }
      }
  }

  private fun MockHttpSession.addSupplierAndDateAttributes(supplier: Supplier?, date: LocalDate?) {
    this.setAttribute("supplier", supplier)
    this.setAttribute("date", date)
  }
}
