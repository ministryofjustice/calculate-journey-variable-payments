package uk.gov.justice.digital.hmpps.pecs.jpc.import.report

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Target(AnnotationTarget.FIELD)
annotation class EventDate

val dateConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
            = cls == LocalDate::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.string != null) {
                LocalDate.parse(jv.string, DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                null
            }

    override fun toJson(o: Any) = """"$o""""
}