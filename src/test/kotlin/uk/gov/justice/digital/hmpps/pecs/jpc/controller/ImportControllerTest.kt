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
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.SpreadsheetProtection
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
// by passing security for now.
@EnableAutoConfiguration(exclude = [ SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class ])
class ImportControllerTest(@Autowired val restTemplate: TestRestTemplate) {

    @MockBean
    lateinit var timeSource: TimeSource

    @SpyBean
    @Autowired
    lateinit var spreadsheetProtection: SpreadsheetProtection

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Test
    fun `can import locations`() {
        whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))

        assertThat(locationRepository.count()).isEqualTo(0)

        val response = restTemplate.getForEntity("/import-locations", InputStreamResource::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(locationRepository.count()).isEqualTo(2)
    }

    @Test
    fun `can import prices`() {
        whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))

        assertThat(priceRepository.count()).isEqualTo(0)

        val response = restTemplate.getForEntity("/import-prices/geoamey", InputStreamResource::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
//        assertThat(priceRepository.count()).isEqualTo(2)
    }

    @Test
    fun `can generate spreadsheet for serco`() {
        whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
        whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

        val response = restTemplate.getForEntity("/generate-prices-spreadsheet/SERCO?moves_from=2020-10-01&moves_to=2020-10-31", InputStreamResource::class.java)

        verify(spreadsheetProtection).protectAndGet(any())
        assertThat(response.headers.contentDisposition.filename).isEqualTo("Journey_Variable_Payment_Output_SERCO_2020-10-13_15_25.xlsx")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `generate-prices-spreadsheet fails if more than one months data is requested`() {
        whenever(timeSource.dateTime()).thenReturn(LocalDateTime.of(2020, 10, 13, 15, 25))
        whenever(timeSource.date()).thenReturn(LocalDate.of(2020, 10, 13))

        val response = restTemplate.getForEntity("/generate-prices-spreadsheet/SERCO?moves_from=2020-10-01&moves_to=2020-11-01", ErrorResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response?.body?.developerMessage).isEqualTo("A maximum of one month's data can be queried at a time.")
    }
}
