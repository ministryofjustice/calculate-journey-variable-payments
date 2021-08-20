package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(
  name = "AUDIT_EVENTS",
  indexes = [
    Index(
      name = "audit_events_event_type_idx",
      columnList = "event_type",
      unique = false
    ),
    Index(
      name = "audit_events_et_mdk_idx",
      columnList = "event_type, metadata_key",
      unique = false
    )
  ]
)
data class AuditEvent(
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  val eventType: AuditEventType,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime,

  @Column(name = "username", nullable = false)
  @get: NotBlank(message = "User cannot be blank")
  val username: String,

  @Column(name = "metadata", nullable = true, length = 1024)
  val metadata: String?,

  @Column(name = "metadata_key", nullable = true, length = 255)
  val metadataKey: String? = null,

  @Id
  @Column(name = "audit_event_id", nullable = false)
  val id: UUID = UUID.randomUUID(),
) {
  constructor(eventType: AuditEventType, createdAt: LocalDateTime, username: String, metadata: Metadata?) : this(
    eventType = eventType,
    createdAt = createdAt,
    username = username,
    metadata = metadata?.toJsonString(),
    metadataKey = metadata?.key()
  )
}

fun interface Metadata {
  fun toJsonString(): String

  /**
   * Provides a constant structured value to aid querying of metadata. If querying is required then this should be overridden.
   */
  fun key(): String? = null
}
