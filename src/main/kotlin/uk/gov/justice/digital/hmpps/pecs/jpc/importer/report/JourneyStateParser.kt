package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyState

@Target(AnnotationTarget.FIELD)
annotation class JourneyStateParser

val journeyStateConverter = object : Converter {

  override fun canConvert(cls: Class<*>) = cls == JourneyState::class.java

  override fun fromJson(jv: JsonValue) = JourneyState.valueOfCaseInsensitive(jv.string)

  override fun toJson(value: Any) =
    """"$value""""
}
