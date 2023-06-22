package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import com.beust.klaxon.Klaxon
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

data class AuditableEvent(
  val type: AuditEventType,
  val username: String,
  val metadata: Metadata? = null,
) {
  constructor(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) : this(
    type,
    username,
    metadata?.let { MetadataWrapper(it) },
  )

  companion object {
    private val terminal = "_TERMINAL_"

    fun isSystemGenerated(event: AuditEvent) = event.username.uppercase().trim() == terminal

    private fun createEvent(
      type: AuditEventType,
      authentication: Authentication? = null,
      metadata: Map<String, Any>? = null,
      allowNoUser: Boolean = false,
    ): AuditableEvent {
      if (authentication == null && !allowNoUser) {
        throw RuntimeException("Attempted to create audit event $type without a user")
      }

      return AuditableEvent(
        type,
        authentication?.name ?: terminal,
        metadata = metadata?.let { MetadataWrapper(it) },
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
        mapOf("month" to date.format(DateTimeFormatter.ofPattern("yyyy-MM")), "supplier" to supplier),
      )

    fun downloadSpreadsheetFailure(date: LocalDate, supplier: Supplier, authentication: Authentication) =
      createEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET_FAILURE,
        authentication,
        mapOf("month" to date.format(DateTimeFormatter.ofPattern("yyyy-MM")), "supplier" to supplier),

      )

    /**
     * A price can be added via the front end in which case it is authenticated or by the back end via bulk price import which is un-authenticated.
     */
    fun addPrice(newPrice: Price): AuditableEvent {
      return AuditableEvent(
        type = AuditEventType.JOURNEY_PRICE,
        username = authentication()?.name ?: terminal,
        metadata = PriceMetadata.new(newPrice),
      )
    }

    fun updatePrice(updatedPrice: Price, oldPrice: Money): AuditableEvent {
      return AuditableEvent(
        type = AuditEventType.JOURNEY_PRICE,
        username = authentication()?.name ?: terminal,
        metadata = PriceMetadata.update(oldPrice, updatedPrice),
      )
    }

    fun addPriceException(price: Price, month: Month, amount: Money): AuditableEvent {
      return AuditableEvent(
        type = AuditEventType.JOURNEY_PRICE,
        username = enforcedAuthentication().name,
        metadata = PriceMetadata.exception(price, month, amount),
      )
    }

    fun removePriceException(price: Price, month: Month, amount: Money): AuditableEvent {
      return AuditableEvent(
        type = AuditEventType.JOURNEY_PRICE,
        username = enforcedAuthentication().name,
        metadata = PriceMetadata.removeException(price, month, amount),
      )
    }

    fun adjustPrice(
      price: Price,
      original: Money,
      multiplier: AdjustmentMultiplier,
      authentication: Authentication? = null,
    ): AuditableEvent {
      return AuditableEvent(
        type = AuditEventType.JOURNEY_PRICE,
        username = authentication?.name ?: terminal,
        metadata = PriceMetadata.adjustment(price, original, multiplier),
      )
    }

    fun journeyPriceBulkPriceAdjustmentEvent(
      supplier: Supplier,
      effectiveYear: Int,
      multiplier: AdjustmentMultiplier,
      authentication: Authentication? = null,
      details: String,
    ) = AuditableEvent(
      type = AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
      username = authentication?.name ?: terminal,
      metadata = AnnualPriceAdjustmentMetadata(supplier, effectiveYear, multiplier.value, details),
    )

    fun mapLocation(location: Location) =
      AuditableEvent(
        type = AuditEventType.LOCATION,
        username = enforcedAuthentication().name,
        metadata = MapLocationMetadata.map(location),
      )

    fun remapLocation(old: Location, new: Location) =
      AuditableEvent(
        type = AuditEventType.LOCATION,
        username = enforcedAuthentication().name,
        metadata = MapLocationMetadata.remap(old, new),
      )

    fun autoMapLocation(location: Location) =
      AuditableEvent(
        type = AuditEventType.LOCATION,
        username = terminal,
        metadata = MapLocationMetadata.map(location),
      )

    fun authentication(): Authentication? = SecurityContextHolder.getContext().authentication

    private fun enforcedAuthentication() = authentication()
      ?: throw RuntimeException("Attempted to create audit event $AuditEventType without a user")

    fun importReportsEvent(
      reportDate: LocalDate,
      moves_processed: Int,
      moves_saved: Int,
      people_processed: Int,
      people_saved: Int,
      profiles_processed: Int,
      profiles_saved: Int,
    ) =
      createEvent(
        type = AuditEventType.REPORTING_DATA_IMPORT,
        metadata = mapOf(
          "report_date" to reportDate.toString(),
          "moves_processed" to moves_processed,
          "moves_saved" to moves_saved,
          "people_processed" to people_processed,
          "people_saved" to people_saved,
          "profiles_processed" to profiles_processed,
          "profiles_saved" to profiles_saved,
        ),
        allowNoUser = true,
      )
  }
}
