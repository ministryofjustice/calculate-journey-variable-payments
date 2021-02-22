package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository, private val timeSource: TimeSource) {
  fun create(event: AuditableEvent) {
    auditEventRepository.save(
      AuditEvent(
        event.type,
        timeSource.dateTime(),
        event.username.trim().toUpperCase(),
        event.extras?.let { Klaxon().toJsonString(it) }
      )
    )
  }
}
