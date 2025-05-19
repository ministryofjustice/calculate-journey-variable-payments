package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.json.BigDecimalParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.json.bigDecimalConverter
import java.math.BigDecimal
import java.time.Month

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
  @BigDecimalParser
  val newPrice: BigDecimal,

  @Json(name = "old_price", index = 6, serializeNull = false)
  @BigDecimalParser
  val oldPrice: BigDecimal? = null,

  @Json(name = "multiplier", index = 7, serializeNull = false)
  @BigDecimalParser
  val multiplier: BigDecimal? = null,

  @Json(name = "exception_month", index = 8)
  val exceptionMonth: String? = null,

  @Json(name = "exception_deleted", index = 9)
  val exceptionDeleted: Boolean? = null,
) : Metadata {
  private constructor(price: Price) : this(
    price.supplier,
    price.fromLocation.nomisAgencyId,
    price.toLocation.nomisAgencyId,
    price.effectiveYear,
    newPrice = price.price().pounds(),
  )

  private constructor(price: Price, exception: Month, exceptionAmount: Money, deleted: Boolean = false) : this(
    price.supplier,
    price.fromLocation.nomisAgencyId,
    price.toLocation.nomisAgencyId,
    price.effectiveYear,
    oldPrice = price.price().pounds(),
    newPrice = exceptionAmount.pounds(),
    exceptionMonth = exception.name,
    exceptionDeleted = deleted,
  )

  private constructor(old: Money, new: Price) : this(
    supplier = new.supplier,
    fromNomisId = new.fromLocation.nomisAgencyId,
    toNomisId = new.toLocation.nomisAgencyId,
    effectiveYear = new.effectiveYear,
    newPrice = new.price().pounds(),
    oldPrice = old.pounds(),
  )

  private constructor(old: Money, new: Price, multiplier: AdjustmentMultiplier) : this(
    supplier = new.supplier,
    fromNomisId = new.fromLocation.nomisAgencyId,
    toNomisId = new.toLocation.nomisAgencyId,
    effectiveYear = new.effectiveYear,
    newPrice = new.price().pounds(),
    oldPrice = old.pounds(),
    multiplier = multiplier.value,
  )

  companion object {
    fun new(price: Price) = PriceMetadata(price)

    fun update(old: Money, new: Price): PriceMetadata {
      if (old == new.price()) throw IllegalArgumentException("Old price and new price are the same '$old'.")

      return PriceMetadata(old, new)
    }

    fun adjustment(new: Price, old: Money, multiplier: AdjustmentMultiplier) = PriceMetadata(old, new, multiplier)

    fun exception(price: Price, month: Month, amount: Money): PriceMetadata = PriceMetadata(price, month, amount)

    fun removeException(price: Price, month: Month, amount: Money): PriceMetadata = PriceMetadata(price, month, amount, true)

    fun map(event: AuditEvent): PriceMetadata = if (event.eventType == AuditEventType.JOURNEY_PRICE) {
      Klaxon().fieldConverter(BigDecimalParser::class, bigDecimalConverter).parse<PriceMetadata>(event.metadata!!)!!
    } else {
      throw IllegalArgumentException("Audit event type is not a price event.")
    }

    fun key(supplier: Supplier, fromNomisId: String, toNomisId: String) = "$supplier-${fromNomisId.trim().uppercase()}-${toNomisId.trim().uppercase()}"
  }

  fun isUpdate() = oldPrice != null && multiplier == null && exceptionMonth == null

  fun isAdjustment() = multiplier != null

  fun isAddException() = exceptionMonth != null && exceptionDeleted == false

  fun isRemoveException() = exceptionMonth != null && exceptionDeleted == true

  override fun toJsonString(): String = Klaxon().fieldConverter(BigDecimalParser::class, bigDecimalConverter).toJsonString(this)

  override fun key(): String = key(supplier, fromNomisId, toNomisId)
}
