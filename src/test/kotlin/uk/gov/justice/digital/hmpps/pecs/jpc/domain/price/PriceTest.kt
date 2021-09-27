package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.Month.DECEMBER
import java.time.Month.FEBRUARY
import java.time.Month.JANUARY
import java.time.Month.MARCH

internal class PriceTest {
  @Test
  fun `Aug 31st is in previous year's effective date`() {
    val aug31_20201 = LocalDate.of(2021, 8, 31)
    assertThat(effectiveYearForDate(aug31_20201)).isEqualTo(2020)
  }

  @Test
  fun `Sept 1st is in this year's effective date`() {
    val sept1_2021 = LocalDate.of(2021, 9, 1)
    assertThat(effectiveYearForDate(sept1_2021)).isEqualTo(2021)
  }

  @Test
  fun `new instance is created with relevant attributes changed upon price adjustment`() {
    val originalPrice = Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      effectiveYear = 2020,
      priceInPence = 1000,
      addedAt = LocalDate.of(2020, 7, 27).atStartOfDay()
    )
    val adjustedPrice = originalPrice.adjusted(
      amount = Money(2000),
      effectiveYear = 2021,
      addedAt = LocalDate.of(2021, 7, 27).atStartOfDay()
    )

    with(originalPrice) {
      assertThat(this).isNotSameAs(adjustedPrice)
      assertThat(this).isNotEqualTo(adjustedPrice)

      assertThat(id).isNotEqualTo(adjustedPrice.id)
      assertThat(priceInPence).isNotEqualTo(adjustedPrice.priceInPence)
      assertThat(effectiveYear).isNotEqualTo(adjustedPrice.effectiveYear)
      assertThat(addedAt).isNotEqualTo(adjustedPrice.addedAt)

      assertThat(fromLocation).isEqualTo(adjustedPrice.fromLocation)
      assertThat(toLocation).isEqualTo(adjustedPrice.toLocation)
    }

    with(adjustedPrice) {
      assertThat(priceInPence).isEqualTo(2000)
      assertThat(effectiveYear).isEqualTo(2021)
      assertThat(addedAt).isEqualTo(LocalDate.of(2021, 7, 27).atStartOfDay())
    }
  }

  @Test
  fun `cannot create price less than one pence`() {
    Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      effectiveYear = 2020,
      priceInPence = 1,
      addedAt = LocalDate.of(2020, 7, 27).atStartOfDay()
    )

    assertThatThrownBy {
      Price(
        supplier = Supplier.SERCO,
        fromLocation = mock(),
        toLocation = mock(),
        effectiveYear = 2020,
        priceInPence = 0,
        addedAt = LocalDate.of(2020, 7, 27).atStartOfDay()
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)

    assertThatThrownBy {
      Price(
        supplier = Supplier.SERCO,
        fromLocation = mock(),
        toLocation = mock(),
        effectiveYear = 2020,
        priceInPence = -1,
        addedAt = LocalDate.of(2020, 7, 27).atStartOfDay()
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `price exceptions are added`() {
    val price = Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      priceInPence = 1000,
      effectiveYear = 2021
    )

    assertThat(price.exceptions()).isEmpty()

    price
      .addException(JANUARY, Money(1))
      .addException(DECEMBER, Money(12))
      .addException(FEBRUARY, Money(2))

    assertThat(price.exceptions().map { Pair(Month.of(it.month), it.priceInPence) }).containsExactlyInAnyOrder(
      Pair(JANUARY, 1),
      Pair(FEBRUARY, 2),
      Pair(DECEMBER, 12)
    )
  }

  @Test
  fun `retrieve a price exception from price`() {
    val price = Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      priceInPence = 1000,
      effectiveYear = 2021
    ).addException(JANUARY, Money(1))
      .addException(DECEMBER, Money(12))
      .addException(FEBRUARY, Money(2))

    with(price) {
      assertThat(exceptionFor(JANUARY)?.let { Pair(Month.of(it.month), it.priceInPence) }).isEqualTo(Pair(JANUARY, 1))
      assertThat(exceptionFor(FEBRUARY)?.let { Pair(Month.of(it.month), it.priceInPence) }).isEqualTo(Pair(FEBRUARY, 2))
      assertThat(exceptionFor(DECEMBER)?.let { Pair(Month.of(it.month), it.priceInPence) }).isEqualTo(Pair(DECEMBER, 12))
      assertThat(exceptionFor(MARCH)).isNull()
    }
  }

  @Test
  fun `price exceptions are removed`() {
    val price = Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      priceInPence = 1000,
      effectiveYear = 2021
    )

    assertThat(price.exceptions()).isEmpty()

    price
      .addException(JANUARY, Money(1))
      .addException(DECEMBER, Money(12))
      .addException(FEBRUARY, Money(2))

    assertThat(price.exceptions().map { Month.of(it.month) }).containsExactlyInAnyOrder(JANUARY, FEBRUARY, DECEMBER)

    price.removeException(DECEMBER)

    assertThat(price.exceptions().map { Month.of(it.month) }).containsExactlyInAnyOrder(JANUARY, FEBRUARY)

    price.removeException(JANUARY)

    assertThat(price.exceptions().map { Month.of(it.month) }).containsExactly(FEBRUARY)

    price.removeException(FEBRUARY)

    assertThat(price.exceptions()).isEmpty()
  }

  @Test
  fun `duplicate price exceptions are ignored`() {
    val price = Price(
      supplier = Supplier.SERCO,
      fromLocation = mock(),
      toLocation = mock(),
      priceInPence = 1000,
      effectiveYear = 2021
    )

    assertThat(price.exceptions()).isEmpty()

    price
      .addException(JANUARY, Money(1))
      .addException(JANUARY, Money(1))
      .addException(JANUARY, Money(1))
      .addException(FEBRUARY, Money(2))

    assertThat(price.exceptions().map { Pair(Month.of(it.month), it.priceInPence) }).containsExactlyInAnyOrder(
      Pair(JANUARY, 1),
      Pair(FEBRUARY, 2)
    )
  }
}
