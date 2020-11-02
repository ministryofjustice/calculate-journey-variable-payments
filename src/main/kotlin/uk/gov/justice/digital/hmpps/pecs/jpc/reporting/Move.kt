package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
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
        @Json(name = "date")
        val moveDate: LocalDate? = null,

        @get: NotBlank(message = "status cannot be blank")
        val status: String,

        @Json(name = "from_location")
        val fromNomisAgencyId: String,

        @Json(name = "from_location_type")
        val fromLocationType: String,

        @Json(name = "to_location")
        val toNomisAgencyId: String? = null,

        @Json(name = "to_location_type")
        val toLocationType: String? = null,

        @Json(name = "cancellation_reason")
        val cancellationReason: String? = null,

        @Json(name = "cancellation_reason_comment")
        val cancellationReasonComment: String? = null

) {
    fun toJson() : String{
        return Klaxon().
        fieldConverter(EventDate::class, dateConverter).
        toJsonString(this)
    }
    companion object {
        val CANCELLATION_REASON_CANCELLED_BY_PMU = "cancelled_by_pmu"

        fun fromJson(json: String): Move? {
            return Klaxon().
            fieldConverter(EventDate::class, dateConverter).
            fieldConverter(EventDateTime::class, dateTimeConverter).
            parse<Move>(json)
        }

    }
}

enum class MoveStatus {
    COMPLETED,
    CANCELLED;

    companion object{
        fun valueOfCaseInsensitive(value: String): MoveStatus {
            return valueOf(value.toUpperCase())
        }
    }
}