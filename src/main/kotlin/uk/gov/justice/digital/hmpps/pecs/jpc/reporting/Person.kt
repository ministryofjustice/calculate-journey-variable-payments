package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import javax.validation.constraints.NotBlank

data class Person(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @Json(name = "prison_number")
        val prisonNumber: String,

) {
    companion object {
        fun fromJson(json: String): Person? {
            return Klaxon().
            parse<Person>(json)
        }
    }
}