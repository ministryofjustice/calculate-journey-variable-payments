package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "AUDIT_EVENT_EXTRAS")
class AuditEventExtra(
  @Column(name = "audit_event_id")
  val audit_event_id: UUID,

  @Column(name = "name", nullable = false)
  val name: String,

  @Column(name = "value", nullable = false)
  val value: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "extra_type", nullable = false)
  var extraType: AuditEventExtraType,

  @Id
  @Column(name = "extra_id", nullable = false)
  val id: UUID = UUID.randomUUID()
)
