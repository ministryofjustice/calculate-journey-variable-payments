package uk.gov.justice.digital.hmpps.pecs.jpc.util.json

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.math.BigDecimal

@Target(AnnotationTarget.FIELD)
annotation class BigDecimalParser

val bigDecimalConverter = object : Converter {

  override fun canConvert(cls: Class<*>) = cls == BigDecimal::class.java

  override fun fromJson(jv: JsonValue) = Result.runCatching { BigDecimal(jv.string) }.getOrElse { jv.double!!.toBigDecimal() }

  override fun toJson(value: Any) = """"$value""""
}
