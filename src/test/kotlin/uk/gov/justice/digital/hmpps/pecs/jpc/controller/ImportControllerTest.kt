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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.ImportStatus
import java.time.Clock

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [ImportControllerTest.TestClock::class])
class ImportControllerTest(@Autowired val restTemplate: TestRestTemplate) {

    @TestConfiguration
    class TestClock {
        @Bean
        fun clock() = Clock.systemDefaultZone()
    }

    @Test
    fun `can import locations followed by prices`() {
        val locationsResponse = restTemplate.postForEntity<String>("/locations/import")
        assertThat(locationsResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(locationsResponse.body).isEqualTo(ImportStatus.DONE.name)

        val pricesResponse = restTemplate.postForEntity<String>("/prices/import")
        assertThat(pricesResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(pricesResponse.body).isEqualTo(ImportStatus.DONE.name)
    }
}