package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

/**
 * Metadata to capture price changes, both new and updates.
 */
data class PriceMetadata(
  @Json(name = "supplier", index = 1)
  val supplier: Supplier,

  @Json(name = "from_nomis_id", index = 2)
  val fromNomisId: String,

  @Json(name = "to_nomis_id", index = 3)
  val toNomisId: String,

  @Json(name = "effective_year", index = 4)
  val effectiveYear: Int,

  @Json(name = "new_price", index = 5)
  val newPrice: Double,

  @Json(name = "old_price", index = 6, serializeNull = false)
  val oldPrice: Double? = null,
) : Metadata {
  private constructor(price: Price) : this(
    price.supplier,
    price.fromLocation.nomisAgencyId,
    price.toLocation.nomisAgencyId,
    price.effectiveYear,
    newPrice = price.price().pounds()
  )

  private constructor(old: Money, new: Price) : this(
    supplier = new.supplier,
    fromNomisId = new.fromLocation.nomisAgencyId,
    toNomisId = new.toLocation.nomisAgencyId,
    effectiveYear = new.effectiveYear,
    newPrice = new.price().pounds(),
    oldPrice = old.pounds()
  )

  companion object {
    fun new(price: Price) = PriceMetadata(price)

    fun update(old: Money, new: Price): PriceMetadata {
      if (old == new.price()) throw IllegalArgumentException("Old price and new price are the same '$old'.")

      return PriceMetadata(old, new)
    }

    fun map(event: AuditEvent): PriceMetadata {
      return if (event.eventType == AuditEventType.JOURNEY_PRICE)
        Klaxon().parse<PriceMetadata>(event.metadata!!)!!
      else
        throw IllegalArgumentException("Audit event type is not a price event.")
    }
  }

  fun isUpdate() = oldPrice != null

  override fun toJsonString(): String = Klaxon().toJsonString(this)
}
