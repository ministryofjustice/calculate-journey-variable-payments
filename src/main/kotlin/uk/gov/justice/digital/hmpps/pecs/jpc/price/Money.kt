package uk.gov.justice.digital.hmpps.pecs.jpc.price

/**
 * Simple value object to encapsulate a monetary amount in pence. Negative amounts are not allowed.
 */
data class Money(val pence: Int) {

  init {
    if (pence < 0) throw IllegalArgumentException("money cannot be less than zero")
  }

  fun pounds() = pence.toDouble() / 100

  fun multiplyBy(multiplier: Double): Money = valueOf(pounds() * multiplier)

  override fun toString(): String = "%.2f".format(pounds())

  companion object Factory {
    fun valueOf(pounds: Double) = Money((pounds * 100).toInt())
  }
}
