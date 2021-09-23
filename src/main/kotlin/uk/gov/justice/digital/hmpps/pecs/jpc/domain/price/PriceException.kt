package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * A price within any given effective year can have a maximum of twelve exceptions, one for each month of the year.
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
  }

  private fun failOnZeroOrLessPrice() {
    if (priceInPence < 1) throw IllegalArgumentException("Price in pence must be greater than zero.")
  }
}
