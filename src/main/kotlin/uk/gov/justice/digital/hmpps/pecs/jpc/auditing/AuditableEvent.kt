package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import java.time.LocalDateTime

data class AuditableEvent(
  val type: AuditEventType,
  val username: String,
  val timestamp: LocalDateTime,
  val extras: Map<String, Any> = mapOf()
)
