package uk.gov.justice.digital.hmpps.pecs.jpc.domain

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

@Target(AnnotationTarget.FIELD)
annotation class SupplierParser

val supplierConverter = object : Converter {

  override fun canConvert(cls: Class<*>) = cls == Supplier::class.java

  override fun fromJson(jv: JsonValue) = Supplier.valueOfCaseInsensitive(jv.string)

  override fun toJson(value: Any) =
    """"$value""""
}
