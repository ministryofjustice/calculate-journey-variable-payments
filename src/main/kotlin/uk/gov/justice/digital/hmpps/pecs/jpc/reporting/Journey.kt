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
@Table(name = "JOURNEYS")
data class Journey(

        @EventUUID
        @Id
        @Column(name = "journey_id", nullable = false)
        val id: UUID,

        @EventUUID
        @Json(name = "move_id")
        val moveId: UUID,

        @Column(nullable = false)
        val billable: Boolean,

        @Column(nullable = false)
        @get: NotBlank(message = "state cannot be blank")
        val state: String,

        @Column(nullable = false)
        @get: NotBlank(message = "supplier cannot be blank")
        val supplier: String,

        @EventDateTime
        @Json(name = "client_timestamp")
        val clientTimestamp: LocalDateTime,

        @Json(name = "vehicle_registration")
        val vehicleRegistration: String?,

        @Json(name = "from_location")
        @Column(nullable = false)
        @get: NotBlank(message = "from location cannot be blank")
        val fromLocation: String,

        @Json(name = "to_location")
        val toLocation: String? = null
) {
    companion object {
        fun fromJson(json: String): Journey? {
            return Klaxon().
            fieldConverter(EventDateTime::class, dateTimeConverter).
            fieldConverter(EventUUID::class, uuidConverter).
            parse<Journey>(json)
        }
    }
}