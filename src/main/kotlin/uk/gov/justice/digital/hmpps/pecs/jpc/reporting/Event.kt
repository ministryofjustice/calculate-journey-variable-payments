package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "EVENTS")
data class Event @JvmOverloads constructor(

        @EventUUID
        @Id
        @Column(name = "event_id", nullable = false)
        val id: UUID,

        @Column(nullable = false)
        @get: NotBlank(message = "type cannot be blank")
        val type: String,

        @Json(name = "actioned_by")
        @Column(nullable = false)
        @get: NotBlank(message = "actioned by cannot be blank")
        val actionedBy: String,

        @Json(name = "eventable_type")
        @Column(nullable = false)
        @get: NotBlank(message = "eventable type cannot be blank")
        val eventableType : String,

        @EventUUID
        @Json(name = "eventable_id")
        @Column(nullable = false)
        val eventableId: UUID,

        @Transient
        val details: Map<String, Any>?,

        @Json(ignored = true)
        @Column
        val detailsString: String = details.toString(),

        @EventDateTime
        @Json(name = "occurred_at")
        @Column(nullable = false, columnDefinition = "TIMESTAMP")
        val occurredAt: LocalDateTime,

        @EventDateTime
        @Json(name = "recorded_at")
        @Column(nullable = false, columnDefinition = "TIMESTAMP")
        val recordedAt: LocalDateTime,

        @Column
        val notes: String?,
) {
    companion object {
        fun fromJson(json: String): Event? {
            return Klaxon().
            fieldConverter(EventDateTime::class, dateTimeConverter).
            fieldConverter(EventUUID::class, uuidConverter).
            parse<Event>(json)
        }
    }
}
