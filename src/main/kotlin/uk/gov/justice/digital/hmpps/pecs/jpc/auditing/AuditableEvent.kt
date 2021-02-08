package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import java.time.Clock
import java.time.LocalDateTime

data class AuditableEvent(
  val type: AuditEventType,
  val username: String,
  val timestamp: LocalDateTime,
  val extras: Map<String, Any> = mapOf()
) {
  companion object {
    private fun timeSource(clock: Clock? = Clock.systemDefaultZone()) = TimeSource { LocalDateTime.now(clock) }

    private fun createEvent(
      type: AuditEventType,
      authentication: Authentication?,
      metadata: Map<String, Any> = mapOf(),
      clock: Clock
    ): AuditableEvent {
      return AuditableEvent(
        type,
        authentication?.name ?: "_TERMINAL_",
        timeSource(clock).dateTime(),
        metadata
      )
    }

    fun createLogInEvent(
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOG_IN,
        authentication,
        clock = clock
      )
    }

    fun createLogOutEvent(
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOG_OUT,
        authentication,
        clock = clock
      )
    }

    fun createDownloadSpreadsheetEvent(
      date: String,
      supplier: String,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET,
        authentication,
        mapOf("month" to date, "supplier" to supplier),
        clock
      )
    }

    fun createLocationNameSetEvent(
      nomisId: String,
      name: String,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_NAME_SET,
        authentication,
        mapOf("nomisId" to nomisId, "name" to name),
        clock
      )
    }

    fun createLocationNameChangeEvent(
      nomisId: String,
      oldName: String,
      newName: String,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_NAME_CHANGE,
        authentication,
        mapOf("nomisId" to nomisId, "oldName" to oldName, "newName" to newName),
        clock
      )
    }

    fun createLocationTypeSetEvent(
      nomisId: String,
      type: LocationType,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_TYPE_SET,
        authentication,
        mapOf("nomisId" to nomisId, "type" to type),
        clock
      )
    }

    fun createLocationTypeChangeEvent(
      nomisId: String,
      oldType: LocationType,
      newType: LocationType,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.LOCATION_TYPE_CHANGE,
        authentication,
        mapOf("nomisId" to nomisId, "oldType" to oldType, "newType" to newType),
        clock
      )
    }

    fun createJourneyPriceSetEvent(
      supplier: String,
      fromNomisId: String,
      toNomisId: String,
      price: Money,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.JOURNEY_PRICE_SET,
        authentication,
        mapOf("supplier" to supplier, "fromNomisId" to fromNomisId, "toNomisId" to toNomisId, "price" to price.pounds()),
        clock
      )
    }

    fun createJourneyPriceChangeEvent(
      supplier: String,
      fromNomisId: String,
      toNomisId: String,
      oldPrice: Money,
      newPrice: Money,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.JOURNEY_PRICE_CHANGE,
        authentication,
        mapOf(
          "supplier" to supplier,
          "fromNomisId" to fromNomisId,
          "toNomisId" to toNomisId,
          "oldPrice" to oldPrice.pounds(),
          "newPrice" to newPrice.pounds()
        ),
        clock
      )
    }

    fun createJourneyPriceBulkUpdateEvent(
      supplier: String,
      multiplier: Double,
      authentication: Authentication? = SecurityContextHolder.getContext().authentication,
      clock: Clock = Clock.systemDefaultZone()
    ): AuditableEvent {
      return createEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        authentication,
        mapOf("supplier" to supplier, "multiplier" to multiplier),
        clock
      )
    }
  }
}
