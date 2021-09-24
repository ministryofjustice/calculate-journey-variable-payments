package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import java.time.LocalDateTime
import java.time.Month
import java.util.UUID
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_location_id")
  val fromLocation: Location,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_location_id")
  val toLocation: Location,

  var priceInPence: Int,

  @NotNull
  val addedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "effective_year", nullable = false)
  val effectiveYear: Int,
) {

  @OneToMany(mappedBy = "price", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
  @MapKey(name = "month")
  private val exceptions: MutableMap<Int, PriceException> = mutableMapOf()

  init {
    failOnZeroOrLessPrice()
  }

  private fun failOnZeroOrLessPrice() {
    if (priceInPence < 1) throw IllegalArgumentException("Price in pence must be greater than zero.")
  }

  fun journey() = "${fromLocation.nomisAgencyId}-${toLocation.nomisAgencyId}"

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
    exceptions.putIfAbsent(month.value, PriceException(price = this, month = month.value, priceInPence = amount.pence))

    return this
  }

  fun removeException(month: Month): Price {
    exceptions.remove(month.value)

    return this
  }

  fun exceptions() = exceptions.values.toSet()
}

enum class Supplier {
  SERCO,
  GEOAMEY,
  UNKNOWN;

  companion object {
    fun valueOfCaseInsensitive(value: String?) =
      kotlin.runCatching { valueOf(value!!.uppercase()) }.getOrDefault(UNKNOWN)
  }
}
