package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.EventDateTime
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.SupplierParser
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.dateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.supplierConverter
import java.time.LocalDateTime
import javax.persistence.AttributeConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "EVENTS", indexes = [Index(name = "eventable_id_index", columnList = "eventable_id", unique = false)])
data class Event constructor(

  @Json(name = "id")
  @Id
  @Column(name = "event_id")
  val eventId: String,

  @EventDateTime
  @Json(name = "updated_at")
  @Column(name = "updated_at")
  val updatedAt: LocalDateTime,

  @get: NotBlank(message = "type cannot be blank")
  @Column(name = "event_type")
  val type: String,

  @SupplierParser
  @Enumerated(EnumType.STRING)
  @Column(name = "supplier")
  val supplier: Supplier? = null,

  @Json(name = "eventable_type")
  @get: NotBlank(message = "eventable type cannot be blank")
  @Column(name = "eventable_type")
  val eventableType: String,

  @Json(name = "eventable_id")
  @Column(name = "eventable_id")
  val eventableId: String,

  @Convert(converter = DetailsConverter::class)
  @Column(name = "details")
  val details: Map<String, Any>? = emptyMap(),

  @EventDateTime
  @Json(name = "occurred_at")
  @Column(name = "occurred_at")
  val occurredAt: LocalDateTime,

  @EventDateTime
  @Json(name = "recorded_at")
  @Column(name = "recorded_at")
  val recordedAt: LocalDateTime,

  @Column(nullable = true, length = 1024)
  val notes: String?,
) : Comparable<Event> {

  fun hasType(et: EventType) = type == et.value
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Event

    if (eventId != other.eventId) return false
    if (updatedAt != other.updatedAt) return false
    if (type != other.type) return false
    if (supplier != other.supplier) return false
    if (eventableType != other.eventableType) return false
    if (eventableId != other.eventableId) return false
    if (occurredAt != other.occurredAt) return false
    if (recordedAt != other.recordedAt) return false
    if (notes != other.notes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = eventId.hashCode()
    result = 31 * result + updatedAt.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + (supplier?.hashCode() ?: 0)
    result = 31 * result + eventableType.hashCode()
    result = 31 * result + eventableId.hashCode()
    result = 31 * result + occurredAt.hashCode()
    result = 31 * result + recordedAt.hashCode()
    result = 31 * result + (notes?.hashCode() ?: 0)
    return result
  }

  fun vehicleRegistration(): String? = details?.get("vehicle_reg") as String?

  override operator fun compareTo(other: Event): Int {
    return this.occurredAt.compareTo(other.occurredAt)
  }

  companion object {
    fun getLatestByType(events: Collection<Event>, eventType: EventType): Event? =
      events.sortedByDescending { it.occurredAt }.find { it.hasType(eventType) }

    fun fromJson(json: String): Event? {
      return Klaxon().fieldConverter(SupplierParser::class, supplierConverter)
        .fieldConverter(EventDateTime::class, dateTimeConverter).parse<Event>(json)
    }
  }
}

enum class EventType(val value: String) {

  MOVE_START("MoveStart"),
  MOVE_ACCEPT("MoveAccept"),
  MOVE_CANCEL("MoveCancel"),
  MOVE_COMPLETE("MoveComplete"),
  MOVE_LOCKOUT("MoveLockout"),
  MOVE_LODGING_START("MoveLodgingStart"),
  MOVE_LODGING_END("MoveLodgingEnd"),
  MOVE_REDIRECT("MoveRedirect"),
  JOURNEY_START("JourneyStart"),
  JOURNEY_COMPLETE("JourneyComplete"),
  JOURNEY_LOCKOUT("JourneyLockout"),
  JOURNEY_LODGING("JourneyLodging"),
  JOURNEY_CANCEL("JourneyCancel"),
  UNKNOWN("Unknown"),
  ;

  companion object {
    val types = values().map { it.value }
  }
}

@Converter
class DetailsConverter : AttributeConverter<Map<String, Any>, String> {
  override fun convertToDatabaseColumn(details: Map<String, Any>?) =
    details?.let { Klaxon().toJsonString(details) }

  override fun convertToEntityAttribute(details: String?) =
    details?.let { Klaxon().parse<Map<String, Any>>(details) }
}
