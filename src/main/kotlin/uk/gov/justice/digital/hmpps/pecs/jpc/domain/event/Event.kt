package uk.gov.justice.digital.hmpps.pecs.jpc.domain.event

import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonDateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonSupplierConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonDateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonSupplierConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
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

  @JsonDateTimeConverter
  @Json(name = "updated_at")
  @Column(name = "updated_at")
  val updatedAt: LocalDateTime,

  @get: NotBlank(message = "type cannot be blank")
  @Column(name = "event_type")
  val type: String,

  @JsonSupplierConverter
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

  @JsonDetailsConverter
  @Convert(converter = JpaDetailsConverter::class)
  @Column(name = "details")
  val details: Details? = null,

  @JsonDateTimeConverter
  @Json(name = "occurred_at")
  @Column(name = "occurred_at")
  val occurredAt: LocalDateTime,

  @JsonDateTimeConverter
  @Json(name = "recorded_at")
  @Column(name = "recorded_at")
  val recordedAt: LocalDateTime,

  @Column(nullable = true, length = 1024)
  val notes: String?,
) : Comparable<Event> {

  constructor(
    eventId: String,
    updatedAt: LocalDateTime,
    type: String,
    supplier: Supplier?,
    eventableType: String,
    eventableId: String,
    details: Map<String, Any> = emptyMap(),
    occurredAt: LocalDateTime,
    recordedAt: LocalDateTime,
    notes: String?
  ) : this(
    eventId,
    updatedAt,
    type,
    supplier,
    eventableType,
    eventableId,
    if (details.isNotEmpty()) Details(details) else null,
    occurredAt,
    recordedAt,
    notes
  )

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
    if (details != other.details) return false

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
    result = 31 * result + (details?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "Event(eventId='$eventId', updatedAt=$updatedAt, type='$type', supplier=$supplier, eventableType='$eventableType', eventableId='$eventableId', details=$details, occurredAt=$occurredAt, recordedAt=$recordedAt, notes=$notes)"
  }

  fun vehicleRegistration(): String? = details?.attributes?.get("vehicle_reg") as String?

  override operator fun compareTo(other: Event): Int {
    return this.occurredAt.compareTo(other.occurredAt)
  }

  companion object {
    fun getLatestByType(events: Collection<Event>, eventType: EventType): Event? =
      events.sortedByDescending { it.occurredAt }.find { it.hasType(eventType) }

    fun fromJson(json: String): Event? {
      return Klaxon().fieldConverter(JsonSupplierConverter::class, jsonSupplierConverter)
        .fieldConverter(JsonDateTimeConverter::class, jsonDateTimeConverter)
        .fieldConverter(JsonDetailsConverter::class, jsonDetailsConverter)
        .parse<Event>(json)
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
  JOURNEY_LODGING("JourneyLodging"),
  JOURNEY_CANCEL("JourneyCancel"),
  UNKNOWN("Unknown"),
  ;

  companion object {
    val types = values().map { it.value }
  }
}

@Converter
class JpaDetailsConverter : AttributeConverter<Details, String> {
  override fun convertToDatabaseColumn(entity: Details?): String? {
    return entity?.let { Klaxon().toJsonString(it.attributes) }
  }

  override fun convertToEntityAttribute(dbData: String?): Details? {
    return dbData?.let { Klaxon().parse<Map<String, Any>>(it)?.let { attrs -> Details(attrs) } }
  }
}

data class Details(val attributes: Map<String, Any?>)

@Target(AnnotationTarget.FIELD)
annotation class JsonDetailsConverter

val jsonDetailsConverter = object : com.beust.klaxon.Converter {
  override fun canConvert(cls: Class<*>) = cls == Details::class.java

  override fun fromJson(jv: JsonValue) = jv.obj?.map?.let { Details(it.toMap()) }

  override fun toJson(value: Any) =
    """"$value""""
}
