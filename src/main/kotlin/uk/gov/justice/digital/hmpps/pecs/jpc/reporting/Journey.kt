package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon

data class Journey(
        val id: String,

        @Json(name = "move_id")
        val moveId: String,
        val billable: Boolean,
        val state: String,
        val supplier: String,

        @Json(name = "vehicle_registration")
        val vehicleRegistration: String,

        @Json(name = "from_location")
        val fromLocation: String,

        @Json(name = "to_location")
        val toLocation: String? = null
) {
    companion object {
        fun fromJson(json: String): Journey? {
            return Klaxon().parse<Journey>(json)
        }
    }
}