package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "AUDIT_EVENTS")
data class AuditEvent(
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  var eventType: AuditEventType,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime,

  @Column(name = "username", nullable = false)
  @get: NotBlank(message = "User cannot be blank")
  val username: String,

  @Id
  @Column(name = "audit_event_id", nullable = false)
  val id: UUID = UUID.randomUUID()
) {
  @OneToMany(orphanRemoval = true)
  @JoinColumn(name = "audit_event_id")
  val extras = mutableListOf<AuditEventExtra>()
}
