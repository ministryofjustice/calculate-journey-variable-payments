package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.time.Month
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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
}