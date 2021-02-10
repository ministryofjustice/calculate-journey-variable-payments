package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AuditableEvent(
  val type: AuditEventType,
  val username: String,
  val timestamp: LocalDateTime,
  val extras: Map<String, Any>? = null
) {
  companion object {
    private fun createEvent(
      type: AuditEventType,
      timeSource: TimeSource,
      authentication: Authentication?,
      metadata: Map<String, Any>? = null
    ): AuditableEvent {
      return AuditableEvent(
        type,
        authentication?.name ?: "_TERMINAL_",
        timeSource.dateTime(),
        metadata
      )
    }

    fun createLogInEvent(
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOG_IN,
        timeSource,
        authentication,
      )
    }

    fun createLogOutEvent(
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOG_OUT,
        timeSource,
        authentication,
      )
    }

    fun createDownloadSpreadsheetEvent(
      date: LocalDate,
      supplier: String,
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET,
        timeSource,
        authentication,
        mapOf("month" to date.format(DateTimeFormatter.ofPattern("yyyy-MM")), "supplier" to supplier)
      )
    }

    fun createLocationNameEvent(
      nomisId: String,
      name: String,
      newName: String? = null,
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_NAME,
        timeSource,
        authentication,
        if (newName == null) {
          mapOf("nomisId" to nomisId, "name" to name)
        } else {
          mapOf("nomisId" to nomisId, "oldName" to name, "newName" to newName)
        }
      )
    }

    fun createLocationTypeEvent(
      nomisId: String,
      type: LocationType,
      newType: LocationType? = null,
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_TYPE,
        timeSource,
        authentication,
        if (newType == null) {
          mapOf("nomisId" to nomisId, "type" to type)
        } else {
          mapOf("nomisId" to nomisId, "oldType" to type, "newType" to newType)
        }
      )
    }

    fun createJourneyPriceEvent(
      supplier: Supplier,
      fromNomisId: String,
      toNomisId: String,
      price: Money,
      newPrice: Money? = null,
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.JOURNEY_PRICE,
        timeSource,
        authentication,
        if (newPrice == null) {
          mapOf(
            "supplier" to supplier,
            "fromNomisId" to fromNomisId,
            "toNomisId" to toNomisId,
            "price" to price.pounds()
          )
        } else {
          mapOf(
            "supplier" to supplier,
            "fromNomisId" to fromNomisId,
            "toNomisId" to toNomisId,
            "oldPrice" to price.pounds(),
            "newPrice" to newPrice.pounds()
          )
        }
      )
    }

    fun createJourneyPriceBulkUpdateEvent(
      supplier: Supplier,
      multiplier: Double,
      timeSource: TimeSource,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
    ): AuditableEvent {
      return createEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        timeSource,
        authentication,
        mapOf("supplier" to supplier, "multiplier" to multiplier)
      )
    }
  }
}
