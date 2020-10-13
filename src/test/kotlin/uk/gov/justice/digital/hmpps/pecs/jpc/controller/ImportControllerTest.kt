package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ImportControllerTest(@Autowired val restTemplate: TestRestTemplate, @Autowired private val clock: Clock) {

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Test
    fun `can generate spreadsheet for serco`() {
        assertThat(locationRepository.count()).isEqualTo(0)
        assertThat(priceRepository.count()).isEqualTo(0)

        val response = restTemplate.getForEntity("/generate-prices-spreadsheet/SERCO?moves_from=2020-10-01&moves_to=2020-10-01&reports_to=2020-10-01", InputStreamResource::class.java)
        val uploadDateTime = LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("YYYY-MM-DD_HH_MM"))

        assertThat(response.headers.contentDisposition.filename).isEqualTo("Journey_Variable_Payment_Output_SERCO_${uploadDateTime}.xlsx")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(locationRepository.count()).isEqualTo(2)
        assertThat(priceRepository.count()).isEqualTo(1)
    }
}
