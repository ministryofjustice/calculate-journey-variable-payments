package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveStatus

@Target(AnnotationTarget.FIELD)
annotation class MoveStatusParser

val moveStatusConverter = object: Converter {

    override fun canConvert(cls: Class<*>) = cls == MoveStatus::class.java

    override fun fromJson(jv: JsonValue) = MoveStatus.valueOfCaseInsensitive(jv.string)

    override fun toJson(value: Any) = """"${value}""""
}