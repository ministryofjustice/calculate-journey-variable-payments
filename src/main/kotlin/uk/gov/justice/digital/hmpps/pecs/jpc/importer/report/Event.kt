package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "EVENTS")
data class Event constructor(

        @get: NotBlank(message = "id cannot be blank")
        @Id
        @Column(name = "event_id")
        val id: String,

        @EventDateTime
        @Json(name = "updated_at")
        @Column(name = "updated_at")
        val updatedAt: LocalDateTime,

        @get: NotBlank(message = "type cannot be blank")
        @Column(name = "event_type")
        val type: String,

        @Transient
        val supplier: String? = null,

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

        val notes: String?,
) {

    fun hasType(et: EventType) = type == et.value

    companion object {
        fun     getLatestByType(events: List<Event>, eventType: EventType): Event? =
                events.sortedByDescending { it.occurredAt }. find { it.hasType(eventType) }

        fun fromJson(json: String): Event? {
            return Klaxon().
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
