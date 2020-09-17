package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.NotBlank

data class Event @JvmOverloads constructor(

        val id: String,

        @get: NotBlank(message = "type cannot be blank")
        val type: String,

        @Json(name = "actioned_by")
        @get: NotBlank(message = "actioned by cannot be blank")
        val actionedBy: String,

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
    companion object {
        fun fromJson(json: String): Event? {
            return Klaxon().
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Event>(json)
        }
    }
}
