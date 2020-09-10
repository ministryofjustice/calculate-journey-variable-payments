package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon

open class Reportable(
    val id: String,
)
{
companion object {
    fun fromJson(json: String) : Reportable? {
        return Klaxon().parse<Reportable>(json)
    }
}
}
