package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AuditableEvent(
  val type: AuditEventType,
  val username: String,
  val extras: Map<String, Any>? = null
) {
  companion object {
    private fun createEvent(
      type: AuditEventType,
      authentication: Authentication? = null,
      metadata: Map<String, Any>? = null,
      allowNoUser: Boolean = false
    ): AuditableEvent {
      if (authentication == null && !allowNoUser)
        throw RuntimeException("Attempted to create audit event $type without a user")

      return AuditableEvent(
        type,
        authentication?.name ?: "_TERMINAL_",
        metadata
      )
    }

    fun logInEvent(authentication: Authentication) = createEvent(AuditEventType.LOG_IN, authentication)

    fun logOutEvent(authentication: Authentication) = createEvent(AuditEventType.LOG_OUT, authentication)

    fun downloadSpreadsheetEvent(
      date: LocalDate,
      supplier: Supplier,
      authentication: Authentication
    ) =
      createEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET,
        authentication,
        mapOf("month" to date.format(DateTimeFormatter.ofPattern("yyyy-MM")), "supplier" to supplier)
      )

    fun addPriceEvent(
      newPrice: Price,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      val metadata = mutableMapOf<String, Any>(
        "supplier" to newPrice.supplier,
        "from_nomis_id" to newPrice.fromLocation.nomisAgencyId,
        "to_nomis_id" to newPrice.toLocation.nomisAgencyId,
        "effective_year" to newPrice.effectiveYear,
        "price" to newPrice.price().pounds()
      )

      return createEvent(AuditEventType.JOURNEY_PRICE, authentication, metadata)
    }

    fun updatePriceEvent(
      updatedPrice: Price,
      oldPrice: Money,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      val metadata = mutableMapOf<String, Any>(
        "supplier" to updatedPrice.supplier,
        "from_nomis_id" to updatedPrice.fromLocation.nomisAgencyId,
        "to_nomis_id" to updatedPrice.toLocation.nomisAgencyId,
        "effective_year" to updatedPrice.effectiveYear,
        "old_price" to oldPrice.pounds(),
        "new_price" to updatedPrice.price().pounds()
      )

      return createEvent(AuditEventType.JOURNEY_PRICE, authentication, metadata)
    }

    fun journeyPriceBulkUpdateEvent(
      supplier: Supplier,
      multiplier: Double,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ) = createEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      authentication,
      mapOf("supplier" to supplier, "multiplier" to multiplier),
      true
    )

    fun locationEvent(
      oldLocation: Location,
      newLocation: Location? = null,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication
    ): AuditableEvent? {
      if (oldLocation.siteName == newLocation?.siteName && oldLocation.locationType == newLocation.locationType) {
        return null
      }

      val metadata = mutableMapOf<String, Any>("nomis_id" to oldLocation.nomisAgencyId)
      if (newLocation != null) {
        if (oldLocation.siteName != newLocation.siteName) {
          metadata["old_name"] = oldLocation.siteName
          metadata["new_name"] = newLocation.siteName
        }

        if (oldLocation.locationType != newLocation.locationType) {
          metadata["old_type"] = oldLocation.locationType
          metadata["new_type"] = newLocation.locationType
        }
      } else {
        metadata["name"] = oldLocation.siteName
        metadata["type"] = oldLocation.locationType
      }

      return createEvent(
        AuditEventType.LOCATION,
        authentication,
        metadata
      )
    }

    fun importReportEvent(type: String, reportDate: LocalDate, processed: Int, saved: Int) =
      createEvent(
        type = AuditEventType.REPORTING_DATA_IMPORT,
        metadata = mapOf(
          "type" to type,
          "report_date" to reportDate.toString(),
          "processed" to processed,
          "saved" to saved

        ),
        allowNoUser = true
      )
  }
}
