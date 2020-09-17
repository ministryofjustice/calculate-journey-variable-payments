package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotBlank

data class Move(

        val id: String,

        @get: NotBlank(message = "reference cannot be blank")
        val reference: String,

        @EventDate
        val date: LocalDate? = null,

        @get: NotBlank(message = "status cannot be blank")
        val status: String,

        @Json(name = "from_location")
        @get: NotBlank(message = "from location cannot be blank")
        val fromLocation: String,

        @Json(name = "to_location")
        val toLocation: String? = null,

) {
    companion object {
        fun fromJson(json: String): Move? {
            return Klaxon().
            fieldConverter(EventDate::class, dateConverter).
            parse<Move>(json)
        }
    }
}