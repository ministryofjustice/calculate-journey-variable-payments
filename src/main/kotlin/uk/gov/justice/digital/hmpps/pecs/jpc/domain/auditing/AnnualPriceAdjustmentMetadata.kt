package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.json.BigDecimalParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.json.bigDecimalConverter
import java.math.BigDecimal

/**
 * Metadata to capture annual price adjustment changes.
 */
data class AnnualPriceAdjustmentMetadata(
  @Json(name = "supplier", index = 1)
  val supplier: Supplier,

  @Json(name = "effective_year", index = 2)
  val effectiveYear: Int,

  @BigDecimalParser
  @Json(name = "multiplier", index = 3)
  val multiplier: BigDecimal,

  @Json(name = "details", index = 4)
  val details: String

) : Metadata {
  companion object {

    fun map(event: AuditEvent): AnnualPriceAdjustmentMetadata {
      return if (event.eventType == AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT)
        Klaxon().fieldConverter(BigDecimalParser::class, bigDecimalConverter).parse<AnnualPriceAdjustmentMetadata>(event.metadata!!)!!
      else
        throw IllegalArgumentException("Audit event type is not a price event.")
    }
  }

  override fun toJsonString(): String = Klaxon().fieldConverter(BigDecimalParser::class, bigDecimalConverter).toJsonString(this)

  override fun key(): String = supplier.name
}
