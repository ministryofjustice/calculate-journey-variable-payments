package uk.gov.justice.digital.hmpps.pecs.jpc.import.report

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import javax.validation.constraints.NotBlank

data class ReportProfile(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @get: NotBlank(message = "person id cannot be blank")
        @Json(name = "person_id")
        val personId: String,

) {
    companion object {
        fun fromJson(json: String): ReportProfile? {
            return Klaxon().
            parse<ReportProfile>(json)
        }
    }
}