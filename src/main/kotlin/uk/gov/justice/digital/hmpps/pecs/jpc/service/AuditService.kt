package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository, private val timeSource: TimeSource) {
  fun create(event: AuditableEvent) {
    auditEventRepository.save(
      AuditEvent(
        eventType = event.type,
        createdAt = timeSource.dateTime(),
        username = event.username.trim().uppercase(),
        metadata = event.metadata
      )
    )
  }

  internal fun auditEventsByTypeAndMetaKey(type: AuditEventType, metaKey: String) = auditEventRepository.findByEventTypeAndMetadataKey(type, metaKey.trim().uppercase())
}
