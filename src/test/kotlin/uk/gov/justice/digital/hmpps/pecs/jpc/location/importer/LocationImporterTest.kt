package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocationImporterTest(
        @Autowired val eventPublisher: ApplicationEventPublisher,
        @Autowired val repo: LocationRepository) {

    @Test
    fun `Assert hello content and status code`() {
        val locationsImporter = LocationsImporter(repo, eventPublisher)

//       locationsImporter.import("classpath:locations.xlsx")
//
//       Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
//        Assertions.assertThat(entity.body).contains("<h1>Spring Boot Web Thymeleaf Example</h1>")
    }
}