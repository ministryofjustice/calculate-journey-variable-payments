package uk.gov.justice.digital.hmpps.pecs.jpc.migrations

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.test.FlywayTestExecutionListener
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestExecutionListeners(
  listeners = [
    DependencyInjectionTestExecutionListener::class,
    FlywayTestExecutionListener::class
  ]
)
@TestPropertySource(
  properties = [
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "spring.flyway.baseline-on-migrate=true"
  ]
)
class MigrationsTest(@Autowired val auditEventRepository: AuditEventRepository) {

  @FlywayTest(locationsForMigrate = ["/db/migration/testdata"])
  @Test
  fun `location audit event metadata can be deserialized after update to metadata structure`() {
    val metadata = MapLocationMetadata.map(auditEventRepository.findById(UUID.fromString("74a1fa0b-d164-49eb-9e7a-4a5b620373a9")).get())

    assertThat(metadata).isEqualTo(MapLocationMetadata("GNI", "GREAT NINE ITERATE", LocationType.MC))
  }
}
