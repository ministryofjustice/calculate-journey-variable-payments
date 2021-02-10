package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository) {
  fun create(event: AuditableEvent) {
    auditEventRepository.save(
      AuditEvent(
        event.type,
        event.timestamp,
        event.username.trim().toUpperCase(),
        if (event.extras != null) {
          Klaxon().toJsonString(event.extras)
        } else {
          null
        }
      )
    )
  }
}
