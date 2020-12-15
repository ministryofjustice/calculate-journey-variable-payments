package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "PEOPLE")
data class Person(

        @Json(name = "id")
        @Id
        @Column(name = "person_id")
        val personId: String,

        @EventDateTime
        @Json(name = "updated_at")
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime,

        @Json(name = "prison_number")
        @Column(name="prison_number")
        val prisonNumber: String?,

        @Json(name="latest_nomis_booking_id")
        @Column(name="latest_nomis_booking_id")
        val latestNomisBookingId: Int? = null,

        @Json(name = "first_names")
        @Column(name = "first_names")
        val firstNames: String? = null,

        @Json(name = "last_name")
        @Column(name = "last_name")
        val lastName: String? = null,

        @EventDate
        @Json(name = "date_of_birth")
        @Column(name = "date_of_birth")
        val dateOfBirth: LocalDate? = null,

        @Json(name = "gender")
        @Column(name = "gender")
        val gender: String? = null,

        @Json(name = "ethnicity")
        @Column(name = "ethnicity")
        val ethnicity: String? = null

        ) {
    companion object {
        fun fromJson(json: String): Person? {
            return Klaxon().
            fieldConverter(EventDate::class, dateConverter).
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Person>(json)
        }
    }
}