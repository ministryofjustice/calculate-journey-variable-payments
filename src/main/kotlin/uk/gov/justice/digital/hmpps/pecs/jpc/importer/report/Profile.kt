package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

data class Profile(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @EventDateTime
        @Json(name = "updated_at")
        val updatedAt: LocalDateTime,

        @get: NotBlank(message = "person id cannot be blank")
        @Json(name = "person_id")
        val personId: String,

        ) {
    companion object {
        fun fromJson(json: String): Profile? {
            return Klaxon().
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Profile>(json)
        }
    }
}