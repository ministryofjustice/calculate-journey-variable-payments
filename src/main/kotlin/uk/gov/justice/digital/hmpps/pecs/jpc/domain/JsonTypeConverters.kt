package uk.gov.justice.digital.hmpps.pecs.jpc.domain

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Contains common shared JSON type converters for converting to domain entity field types.
 */

@Target(AnnotationTarget.FIELD)
annotation class JsonDateConverter

val jsonDateConverter = object : Converter {
  override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

  override fun fromJson(jv: JsonValue) =
    if (jv.string != null) {
      LocalDate.parse(jv.string, DateTimeFormatter.ISO_LOCAL_DATE)
    } else {
      null
    }

  override fun toJson(value: Any) =
    """"$value""""
}

@Target(AnnotationTarget.FIELD)
annotation class JsonDateTimeConverter

val jsonDateTimeConverter = object : Converter {
  override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

  override fun fromJson(jv: JsonValue) =
    if (jv.string != null) {
      LocalDateTime.parse(jv.string, DateTimeFormatter.ISO_DATE_TIME)
    } else {
      throw KlaxonException("Couldn't parse date: ${jv.string}")
    }

  override fun toJson(value: Any) =
    """"$value""""
}

@Target(AnnotationTarget.FIELD)
annotation class JsonSupplierConverter

val jsonSupplierConverter = object : Converter {

  override fun canConvert(cls: Class<*>) = cls == Supplier::class.java

  override fun fromJson(jv: JsonValue) = Supplier.valueOfCaseInsensitive(jv.string)

  override fun toJson(value: Any) =
    """"$value""""
}
