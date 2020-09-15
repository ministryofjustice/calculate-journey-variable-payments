package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import java.util.UUID

@Target(AnnotationTarget.FIELD)
annotation class EventUUID

val uuidConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
            = cls == UUID::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.string != null) {
                UUID.fromString(jv.string)
            } else {
                throw KlaxonException("Couldn't parse UUID: ${jv.string}")
            }

    override fun toJson(o: Any)
            = """ { "id" : $o } """
}