package uk.gov.justice.digital.hmpps.pecs.jpc.price

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull

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
  val effectiveYear: Int
) {
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
