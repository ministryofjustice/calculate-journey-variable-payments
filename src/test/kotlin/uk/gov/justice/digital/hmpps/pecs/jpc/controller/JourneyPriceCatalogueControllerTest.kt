package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class JourneyPriceCatalogueControllerTest(@Autowired val mockMvc: MockMvc) {

  @MockBean
  lateinit var timeSource: TimeSource

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `can generate spreadsheet with correct name for Serco`() {
    whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
    whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

    mockMvc.get("/generate-prices-spreadsheet/serco?moves_from=2020-10-01") { }
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

    mockMvc.get("/generate-prices-spreadsheet/geoamey?moves_from=2020-10-01") { }
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
}
