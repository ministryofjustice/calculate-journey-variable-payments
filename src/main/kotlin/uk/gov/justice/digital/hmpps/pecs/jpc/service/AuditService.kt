package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository, private val timeSource: TimeSource) {
  fun create(event: AuditableEvent) {
    auditEventRepository.save(
      AuditEvent(
        eventType = event.type,
        createdAt = timeSource.dateTime(),
        username = event.username.trim().uppercase(),
        metadata = event.metadata,
      ),
    )
  }

  internal fun auditEventsByTypeAndMetaKey(type: AuditEventType, metaKey: String) = auditEventRepository.findByEventTypeAndMetadataKey(type, metaKey.trim().uppercase())

  fun findMostRecentEventByType(type: AuditEventType) = auditEventRepository.findFirstByEventTypeOrderByCreatedAtDesc(type)
}
