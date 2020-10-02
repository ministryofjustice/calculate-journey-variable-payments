package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

data class Event constructor(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @get: NotBlank(message = "type cannot be blank")
        val type: String,

        val supplier: String? = null,

        @Json(name = "eventable_type")
        @get: NotBlank(message = "eventable type cannot be blank")
        val eventableType : String,

        @Json(name = "eventable_id")
        val eventableId: String,

        val details: Map<String, Any>?,

        @EventDateTime
        @Json(name = "occurred_at")
        val occurredAt: LocalDateTime,

        @EventDateTime
        @Json(name = "recorded_at")
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
    MOVE_CANCEL("MoveCancel"),
    MOVE_COMPLETE("MoveComplete"),
    MOVE_LOCKOUT("MoveLockout"),
    MOVE_LODGING_START("MoveLodgingStart"),
    MOVE_LODGING_END("MoveLodgingEnd"),
    MOVE_REDIRECT("MoveRedirect"),
    JOURNEY_START("JourneyStart"),
    JOURNEY_END("JourneyEnd"),
    JOURNEY_LOCKOUT("JourneyLockout"),
    JOURNEY_LODGING("JourneyLodging"),
    JOURNEY_CANCEL("JourneyCancel"),
    ;

    companion object{
        val types = values().map { it.value }
    }
}
