package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon

data class Move(
        val id: String,
        val reference: String,
        val date: String? = null,
        val status: String,

        @Json(name = "from_location")
        val fromLocation: String,

        @Json(name = "to_location")
        val toLocation: String? = null
) {
    companion object {
        fun fromJson(json: String): Move? {
            return Klaxon().parse<Move>(json)
        }
    }
}