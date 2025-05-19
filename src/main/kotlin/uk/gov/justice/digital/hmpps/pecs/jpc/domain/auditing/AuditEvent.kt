package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
  name = "AUDIT_EVENTS",
  indexes = [
    Index(
      name = "audit_events_event_type_idx",
      columnList = "event_type",
      unique = false,
    ),
    Index(
      name = "audit_events_et_mdk_idx",
      columnList = "event_type, metadata_key",
      unique = false,
    ),
  ],
)
data class AuditEvent(
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  val eventType: AuditEventType,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime,

  @Column(name = "username", nullable = false)
  @get:NotBlank(message = "User cannot be blank")
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
    metadataKey = metadata?.key(),
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AuditEvent

    if (eventType != other.eventType) return false
    if (createdAt != other.createdAt) return false
    if (username != other.username) return false
    if (metadata != other.metadata) return false
    if (metadataKey != other.metadataKey) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = eventType.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + username.hashCode()
    result = 31 * result + (metadata?.hashCode() ?: 0)
    result = 31 * result + (metadataKey?.hashCode() ?: 0)
    result = 31 * result + id.hashCode()
    return result
  }

  override fun toString(): String = "AuditEvent(eventType=$eventType, createdAt=$createdAt, username='$username', metadata=XXXXXX, metadataKey=$metadataKey, id=$id)"
}

fun interface Metadata {
  fun toJsonString(): String

  /**
   * Provides a constant structured value to aid querying of metadata. If querying is required then this should be overridden.
   */
  fun key(): String? = null
}
