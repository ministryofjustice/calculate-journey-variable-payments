package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDateTime

data class PriceHistoryDto(
  val datetime: LocalDateTime,
  val action: String,
  val by: String,
) {
  companion object {
    /**
     * Throws a runtime exception if the [AuditEvent] is not a journey price event or if there is a supplier mismatch.
     */
    fun valueOf(supplier: Supplier, event: AuditEvent): PriceHistoryDto {
      val data = PriceMetadata.map(event)

      if (data.supplier != supplier) throw RuntimeException("Audit priced event not for supplier $supplier")

      val action = buildString {
        append(
          when {
            data.isUpdate() -> "Price changed from £${Money.valueOf(data.oldPrice!!)} to £${Money.valueOf(data.newPrice)}. Effective from ${data.effectiveYear} to ${data.effectiveYear + 1}."
            data.isAdjustment() -> "Price adjusted from £${Money.valueOf(data.oldPrice!!)} to £${Money.valueOf(data.newPrice)} with blended rate multiplier ${data.multiplier}. Effective from ${data.effectiveYear} to ${data.effectiveYear + 1}."
            data.isAddException() -> "Price exception of £${Money.valueOf(data.newPrice)} for ${data.exceptionMonth}. Effective year ${data.effectiveYear} to ${data.effectiveYear + 1}."
            data.isRemoveException() -> "Price exception of £${Money.valueOf(data.newPrice)} for ${data.exceptionMonth} removed. Effective year ${data.effectiveYear} to ${data.effectiveYear + 1}."
            else -> "Journey priced at £${Money.valueOf(data.newPrice)}. Effective from ${data.effectiveYear} to ${data.effectiveYear + 1}."
          },
        )
      }

      return PriceHistoryDto(
        event.createdAt,
        action,
        if (AuditableEvent.isSystemGenerated(event)) "SYSTEM" else event.username,
      )
    }
  }
}
