package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import java.time.Clock

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ImportControllerTest(@Autowired val restTemplate: TestRestTemplate) {

    @Autowired
    lateinit var locationRepository: LocationRepository

    @Autowired
    lateinit var priceRepository: PriceRepository

    @Test
    fun `can import locations followed by prices`() {
        assertThat(locationRepository.count()).isEqualTo(0)
        val locationsResponse = restTemplate.postForEntity<String>("/locations/import")
        assertThat(locationsResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(locationsResponse.body).isEqualTo(ImportStatus.DONE.name)
        assertThat(locationRepository.count()).isEqualTo(2)

        assertThat(priceRepository.count()).isEqualTo(0)
        val pricesResponse = restTemplate.postForEntity<String>("/prices/import")
        assertThat(pricesResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(pricesResponse.body).isEqualTo(ImportStatus.DONE.name)
        assertThat(priceRepository.count()).isEqualTo(2)
    }
}