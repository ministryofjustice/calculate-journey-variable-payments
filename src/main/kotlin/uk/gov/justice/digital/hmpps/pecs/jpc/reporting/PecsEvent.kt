package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import java.util.*

open class PecsEvent(
    val type: String,

    @Json(name = "occurred_at")
    val occurredAt: Date,

    @Json(name = "recorded_at")
    val recordedAt: Date,

    val notes: String,
)
{

}

data class MOVE_REDIRECT(
        val details: Details
){
    data class Details(
            @Json(name = "redirect_reason")
            val redirectReason: String,
    ){
    }
}

//enum class EventType{
//    MOVE_START, MOVE_COMPLETE, MOVE_UNCOMPLETE, MOVE_CANCEL, MOVE_REDIRECT, MOVE_LODGING, MOVE_LOCKOUT,
//    JOURNEY_START, JOURNEY_COMPLETE, JOURNEY_UNCOMPLETE, JOURNEY_CANCEL, JOURNEY_LODGING, JOURNEY_LOCKOUT
//}