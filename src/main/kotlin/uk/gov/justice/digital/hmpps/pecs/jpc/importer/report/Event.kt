package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime
import javax.persistence.*
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
        val eventableType : String,

        @Json(name = "eventable_id")
        @Column(name = "eventable_id")
        val eventableId: String,

        @Transient
        val details: Map<String, Any>?,

        @EventDateTime
        @Json(name = "occurred_at")
        @Column(name = "occurred_at")
        val occurredAt: LocalDateTime,

        @EventDateTime
        @Json(name = "recorded_at")
        @Column(name = "recorded_at")
        val recordedAt: LocalDateTime,

        var notes: String?,
) {

    init {
        notes = notes?.take(255)
    }

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

    companion object {
        fun     getLatestByType(events: Collection<Event>, eventType: EventType): Event? =
                events.sortedByDescending { it.occurredAt }. find { it.hasType(eventType) }

        fun fromJson(json: String): Event? {
            return Klaxon().
            fieldConverter(SupplierParser::class, supplierConverter).
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Event>(json)
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

    companion object{
        val types = values().map { it.value }

        fun valueOfOrUnknown(name: String) = if(EventType.values().any{it.name == name}) EventType.valueOf(name) else UNKNOWN
    }

}
