package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import com.beust.klaxon.Klaxon
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
  val metadata: Metadata? = null
) {
  constructor(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) : this(type, username, metadata?.let { MetadataWrapper(it) })

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
        metadata = metadata?.let { MetadataWrapper(it) }
      )
    }

    private data class MetadataWrapper(private val metadata: Map<String, Any>) : Metadata {
      override fun toJsonString() = Klaxon().toJsonString(metadata)
    }

    fun logInEvent(authentication: Authentication) = createEvent(AuditEventType.LOG_IN, authentication)

    fun logOutEvent(authentication: Authentication) = createEvent(AuditEventType.LOG_OUT, authentication)

    fun downloadSpreadsheetEvent(date: LocalDate, supplier: Supplier, authentication: Authentication) =
      createEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET,
        authentication,
        mapOf("month" to date.format(DateTimeFormatter.ofPattern("yyyy-MM")), "supplier" to supplier)
      )

    fun addPriceEvent(newPrice: Price): AuditableEvent {
      val metadata = mutableMapOf<String, Any>(
        "supplier" to newPrice.supplier,
        "from_nomis_id" to newPrice.fromLocation.nomisAgencyId,
        "to_nomis_id" to newPrice.toLocation.nomisAgencyId,
        "effective_year" to newPrice.effectiveYear,
        "price" to newPrice.price().pounds()
      )

      return createEvent(AuditEventType.JOURNEY_PRICE, authentication(), metadata)
    }

    fun updatePriceEvent(updatedPrice: Price, oldPrice: Money): AuditableEvent {
      val metadata = mutableMapOf<String, Any>(
        "supplier" to updatedPrice.supplier,
        "from_nomis_id" to updatedPrice.fromLocation.nomisAgencyId,
        "to_nomis_id" to updatedPrice.toLocation.nomisAgencyId,
        "effective_year" to updatedPrice.effectiveYear,
        "old_price" to oldPrice.pounds(),
        "new_price" to updatedPrice.price().pounds()
      )

      return createEvent(AuditEventType.JOURNEY_PRICE, authentication(), metadata)
    }

    fun journeyPriceBulkUpdateEvent(
      supplier: Supplier,
      multiplier: Double
    ) = createEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      null,
      mapOf("supplier" to supplier, "multiplier" to multiplier),
      true
    )

    fun mapLocation(location: Location) =
      AuditableEvent(
        type = AuditEventType.LOCATION,
        username = authentication().name,
        metadata = MapLocationMetadata.map(location)
      )

    fun remapLocation(old: Location, new: Location) =
      AuditableEvent(
        type = AuditEventType.LOCATION,
        username = authentication().name,
        metadata = MapLocationMetadata.remap(old, new)
      )

    private fun authentication() = SecurityContextHolder.getContext().authentication
      ?: throw RuntimeException("Attempted to create audit event $AuditEventType.LOCATION without a user")

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
