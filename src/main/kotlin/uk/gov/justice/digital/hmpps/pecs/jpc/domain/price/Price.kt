package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import java.time.LocalDateTime
import java.time.Month
import java.util.UUID
import javax.annotation.Nullable
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapKey
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull

/**
 * A price represents an agreed contractual amount with a supplier for a given journey (from A to B) in an effective year.
 */
@Entity
@Table(
  name = "PRICES",
  indexes = [
    Index(
      name = "supplier_from_to_year_index",
      columnList = "supplier, from_location_id, to_location_id, effective_year",
      unique = true
    )
  ]
)
data class Price(
  @Id
  @Column(name = "price_id", nullable = false)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  val supplier: Supplier,

  @ManyToOne
  @JoinColumn(name = "from_location_id")
  val fromLocation: Location,

  @ManyToOne
  @JoinColumn(name = "to_location_id")
  val toLocation: Location,

  var priceInPence: Int,

  @NotNull
  val addedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "effective_year", nullable = false)
  val effectiveYear: Int,

  @Nullable
  @Transient
  val previousPrice: Int? = null,
) {

  @OneToMany(mappedBy = "price", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @MapKey(name = "month")
  private val exceptions: MutableMap<Int, PriceException> = mutableMapOf()

  init {
    failOnZeroOrLessPrice()
  }

  private fun failOnZeroOrLessPrice() {
    if (priceInPence < 1) throw IllegalArgumentException("Price in pence must be greater than zero.")
  }

  fun price() = Money(priceInPence)

  /**
   * Returns a new instance of the price with the provided adjustments.  The locations remain the same.
   */
  fun adjusted(amount: Money, effectiveYear: Int, addedAt: LocalDateTime) = this.copy(
    id = UUID.randomUUID(),
    priceInPence = amount.pence,
    effectiveYear = effectiveYear,
    addedAt = addedAt
  )

  /**
   * Duplicate months will be ignored. You must remove first.
   */
  fun addException(month: Month, amount: Money): Price {
    if (amount.pence == priceInPence) throw IllegalArgumentException("Price exception price cannot be the same as the actual price.")

    exceptions.putIfAbsent(month.value, PriceException(price = this, month = month.value, priceInPence = amount.pence))

    return this
  }

  fun removeException(month: Month): Price {
    exceptions.remove(month.value)

    return this
  }

  fun exceptions() = exceptions.values.toSet()

  fun exceptionFor(month: Month) = exceptions[month.value]

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Price

    if (id != other.id) return false
    if (supplier != other.supplier) return false
    if (fromLocation != other.fromLocation) return false
    if (toLocation != other.toLocation) return false
    if (priceInPence != other.priceInPence) return false
    if (addedAt != other.addedAt) return false
    if (effectiveYear != other.effectiveYear) return false
    if (exceptions != other.exceptions) return false

    return true
  }

  override fun hashCode() = id.hashCode()

  override fun toString(): String {
    return "Price(id=$id, supplier=$supplier, fromLocation=$fromLocation, toLocation=$toLocation, priceInPence=XXXXXX, addedAt=$addedAt, effectiveYear=$effectiveYear, exceptions=${exceptions.values.map { it.id }})"
  }
}

enum class Supplier {
  SERCO,
  GEOAMEY,
  UNKNOWN;

  companion object {
    fun valueOfCaseInsensitive(value: String?) =
      kotlin.runCatching { valueOf(value!!.uppercase()) }.getOrDefault(UNKNOWN)

    fun forEach(consumer: (Supplier) -> Unit) {
      listOf(SERCO, GEOAMEY).forEach { consumer(it) }
    }
  }
}
