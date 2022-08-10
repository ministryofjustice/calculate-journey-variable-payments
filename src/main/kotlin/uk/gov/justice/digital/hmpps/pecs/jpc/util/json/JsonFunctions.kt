package uk.gov.justice.digital.hmpps.pecs.jpc.util.json

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader
import java.time.LocalDate
import kotlin.reflect.KClass

fun dateFieldFromJson(json: String, fieldName: String): LocalDate? =
  valueOf(toJsonObject(json), fieldName, LocalDate::class)

private inline fun <reified T : Any> valueOf(jsonObject: JsonObject, fieldName: String, type: KClass<T>): T? =
  when (type) {
    LocalDate::class -> jsonObject.localDate(fieldName)
    else -> throw RuntimeException("Unsupported type ${type.simpleName}")
  }?.let { it as T }

private fun toJsonObject(jsonString: String) = Klaxon().parseJsonObject(StringReader(jsonString))

private fun JsonObject.localDate(fieldName: String) = this.string(fieldName)?.let { LocalDate.parse(it) }
