package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ErrorResponse
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.SpreadsheetProtection
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
// bypassing security for now.
@EnableAutoConfiguration(exclude = [ SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class ])
class OutputSpreadsheetControllerTest(@Autowired val restTemplate: TestRestTemplate) {

    @MockBean
    lateinit var timeSource: TimeSource

    @SpyBean
    @Autowired
    lateinit var spreadsheetProtection: SpreadsheetProtection

    @Test
    fun `can generate spreadsheet for serco`() {
        whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
        whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

        val response = restTemplate.getForEntity("/generate-prices-spreadsheet/SERCO?moves_from=2020-10-01", InputStreamResource::class.java)

        verify(spreadsheetProtection).protectAndGet(any())
        assertThat(response.headers.contentDisposition.filename).isEqualTo("Journey_Variable_Payment_Output_SERCO_2020-10-13_15_25.xlsx")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
