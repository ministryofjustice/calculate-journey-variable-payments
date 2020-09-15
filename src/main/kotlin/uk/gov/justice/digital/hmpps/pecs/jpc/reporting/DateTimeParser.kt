package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Target(AnnotationTarget.FIELD)
annotation class EventDateTime

val dateTimeConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
            = cls == LocalDateTime::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.string != null) {
                LocalDateTime.parse(jv.string, DateTimeFormatter.ISO_DATE_TIME)
            } else {
                throw KlaxonException("Couldn't parse date: ${jv.string}")
            }

    override fun toJson(o: Any)
            = """ { "date_time" : $o } """
}