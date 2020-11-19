package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDate
import javax.validation.constraints.NotBlank

data class Person(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @Json(name = "prison_number")
        val prisonNumber: String?,

        @Json(name="latest_nomis_booking_id")
        val latestNomisBookingId: Int? = null,

        @Json(name = "first_names")
        val firstNames: String? = null,

        @Json(name = "last_name")
        val lastName: String? = null,

        @EventDate
        @Json(name = "date_of_birth")
        val dateOfBirth: LocalDate? = null,

        @Json(name = "gender")
        val gender: String? = null,

        @Json(name = "ethnicity")
        val ethnicity: String? = null,

) {
    companion object {
        fun fromJson(json: String): Person? {
            return Klaxon().
            fieldConverter(EventDate::class, dateConverter).
            parse<Person>(json)
        }
    }
}