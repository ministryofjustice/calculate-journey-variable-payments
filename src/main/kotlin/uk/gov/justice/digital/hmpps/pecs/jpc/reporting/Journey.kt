package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

data class Journey(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @Json(name = "move_id")
        val moveId: String,

        val billable: Boolean,

        @get: NotBlank(message = "state cannot be blank")
        val state: String,

        @get: NotBlank(message = "supplier cannot be blank")
        val supplier: String,

        @EventDateTime
        @Json(name = "client_timestamp")
        val clientTimestamp: LocalDateTime,

        @Json(name = "vehicle_registration")
        val vehicleRegistration: String?,

        @Json(name = "from_location")
        val fromLocation: Location,

        @Json(name = "to_location")
        val toLocation: Location
)

{
    fun stateIsAnyOf(vararg states: JourneyState) = states.map{it.value}.contains(state)

    companion object {
        fun fromJson(json: String, locationConverter: Converter): Journey? {
            return Klaxon().
            converter(locationConverter).
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Journey>(json)
        }
    }
}

enum class JourneyState(val value: String) {
    CANCELLED("cancelled"),
    COMPLETED("completed");

    companion object{
        val states = values().map { it.value }
    }
}