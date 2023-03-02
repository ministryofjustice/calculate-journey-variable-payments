package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Month
import java.util.UUID

/**
 * A price exception provides the ability to override a price in a given effective year for a period of one month. This
 * allows for short term price changes if the supplier has to use a different route due to circumstances beyond their
 * control. An example of this could be road closures due to road works so they have to go via a longer route.
 *
 * There can be upto twelve price exceptions (one for each month) for a price in any given year, this is however highly
 * unlikely.
 */
@Entity
@Table(
  name = "PRICE_EXCEPTIONS",
  indexes = [
    Index(
      name = "price_id_month_index",
      columnList = "price_id, month",
      unique = true
    )
  ]
)
data class PriceException(
  @Id
  @Column(name = "price_exception_id", nullable = false)
  val id: UUID = UUID.randomUUID(),

  @ManyToOne
  @JoinColumn(name = "price_id", nullable = false)
  val price: Price,

  @Column(name = "month", nullable = false)
  val month: Int,

  @Column(name = "price_in_pence", nullable = false)
  var priceInPence: Int,
) {

  init {
    failOnZeroOrLessPrice()
    failOnInvalidMonth()
  }

  private fun failOnZeroOrLessPrice() {
    if (priceInPence < 1) throw IllegalArgumentException("Price exception amount must be greater than zero.")
  }

  private fun failOnInvalidMonth() {
    Month.of(month)
  }

  fun price() = Money(priceInPence)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PriceException

    if (id != other.id) return false
    if (price != other.price) return false
    if (month != other.month) return false
    if (priceInPence != other.priceInPence) return false

    return true
  }

  override fun hashCode() = id.hashCode()

  override fun toString(): String {
    return "PriceException(id=$id, price=$price, month=$month, priceInPence=XXXXXX)"
  }
}
