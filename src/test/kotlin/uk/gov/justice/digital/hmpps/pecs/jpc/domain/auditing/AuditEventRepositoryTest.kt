package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
class AuditEventRepositoryTest {

  @Autowired
  lateinit var repository: AuditEventRepository

  @Test
  fun `find latest event by type null by default`() {
    AuditEventType.values().forEach {
      assertThat(repository.findFirstByEventTypeOrderByCreatedAtDesc(it)).isNull()
    }
  }

  @Test
  fun `find latest event by type`() {
    AuditEventType.values().forEach { eventType ->
      val first = AuditEvent(
        eventType = eventType,
        createdAt = LocalDateTime.now(),
        metadata = null,
        metadataKey = null,
        username = "first"
      )

      val second = first.copy(id = UUID.randomUUID(), createdAt = first.createdAt.minusMinutes(1), username = "second")

      repository.saveAll(listOf(first, second))

      assertThat(repository.findFirstByEventTypeOrderByCreatedAtDesc(eventType)).isEqualTo(
        first
      )
    }
  }
}
