package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import java.time.LocalDateTime

data class PriceHistoryDto(val datetime: LocalDateTime, val action: String, val by: String) {
  companion object {
    /**
     * Throws a runtime exception if the [AuditEvent] is not the correct [AuditEventType].
     */
    fun valueOf(event: AuditEvent): PriceHistoryDto {

      val data = PriceMetadata.map(event)

      val action = buildString {
        if (data.isUpdate()) {
          append("Price changed from £${Money.valueOf(data.oldPrice!!)} to £${Money.valueOf(data.newPrice)} for journey from '${data.fromNomisId}' to '${data.toNomisId}'. Effective from ${data.effectiveYear - 1} to ${data.effectiveYear}.")
        } else {
          append("Journey from '${data.fromNomisId}' to '${data.toNomisId}' priced at £${Money.valueOf(data.newPrice)}. Effective from ${data.effectiveYear - 1} to ${data.effectiveYear}.")
        }
      }

      return PriceHistoryDto(
        event.createdAt,
        action,
        if (AuditableEvent.isSystemGenerated(event)) "SYSTEM" else event.username
      )
    }
  }
}
