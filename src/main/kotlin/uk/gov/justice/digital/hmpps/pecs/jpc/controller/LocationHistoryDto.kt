package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.MapLocationMetadata
import java.time.LocalDateTime

data class LocationHistoryDto(val datetime: LocalDateTime, val action: String, val by: String) {
  companion object {
    fun valueOf(event: AuditEvent): LocationHistoryDto {

      val data = MapLocationMetadata.map(event)

      val action = buildString {
        if (data.isRemapping()) {
          if (data.oldName != null) append("Location name changed from '${data.oldName}' to '${data.newName}'")
          if (data.oldType != null && data.oldName == null) append("Location type changed from '${data.oldType.label}' to '${data.newType!!.label}'")
          if (data.oldName != null && data.oldType != null) append(" and location type changed from '${data.oldType.label}' to '${data.newType!!.label}'")
        } else {
          append("Assigned to location name '${data.newName}' and type '${data.newType?.label}'")
        }
      }

      return LocationHistoryDto(
        event.createdAt,
        action,
        if (AuditableEvent.isSystemGenerated(event)) "SYSTEM" else event.username
      )
    }
  }
}
