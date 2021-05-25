package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime

data class PriceHistoryDto(val datetime: LocalDateTime, val action: String, val by: String) {
  companion object {
    /**
     * Throws a runtime exception if the [AuditEvent] is not a journey price event or if there is a supplier mismatch.
     */
    fun valueOf(supplier: Supplier, event: AuditEvent): PriceHistoryDto {

      val data = PriceMetadata.map(event)

      if (data.supplier != supplier) throw RuntimeException("Audit priced event not for supplier $supplier")

      val action = buildString {
        if (data.isUpdate()) {
          append("Price changed from £${Money.valueOf(data.oldPrice!!)} to £${Money.valueOf(data.newPrice)}. Effective from ${data.effectiveYear} to ${data.effectiveYear + 1}.")
        } else {
          append("Journey priced at £${Money.valueOf(data.newPrice)}. Effective from ${data.effectiveYear} to ${data.effectiveYear + 1}.")
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
