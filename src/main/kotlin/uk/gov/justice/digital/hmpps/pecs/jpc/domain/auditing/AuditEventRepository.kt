package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface AuditEventRepository : CrudRepository<AuditEvent, UUID> {
  fun findByEventTypeAndMetadataKey(eventType: AuditEventType, metadataKey: String): List<AuditEvent>

  fun findFirstByEventTypeOrderByCreatedAtDesc(eventType: AuditEventType): AuditEvent?
}
