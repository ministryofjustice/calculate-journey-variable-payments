package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.time.LocalDate
import javax.validation.constraints.NotBlank

data class Move(

        @get: NotBlank(message = "id cannot be blank")
        val id: String,

        @get: NotBlank(message = "supplier cannot be blank")
        val supplier: String,

        @Json(name = "profile_id")
        val profileId: String?,

        @get: NotBlank(message = "reference cannot be blank")
        val reference: String,

        @EventDate
        val date: LocalDate? = null,

        @get: NotBlank(message = "status cannot be blank")
        val status: String,

        @Json(name = "from_location")
        val fromLocation: Location,

        @Json(name = "to_location")
        val toLocation: Location? = null,

        @Json(name = "cancellation_reason")
        val cancellationReason: String? = null,

        @Json(name = "cancellation_reason_comment")
        val cancellationReasonComment: String? = null

) {
    companion object {
        val CANCELLATION_REASON_CANCELLED_BY_PMU = "cancelled_by_pmu"

        fun fromJson(json: String, locationConverter: Converter): Move? {
            return Klaxon().
            converter(locationConverter).
            fieldConverter(EventDate::class, dateConverter).
            parse<Move>(json)
        }
    }
}

enum class MoveStatus(val value: String) {
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object{
        val statuses = values().map { it.value }
    }
}