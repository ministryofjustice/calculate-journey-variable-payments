package uk.gov.justice.digital.hmpps.pecs.jpc.price

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
    val originalPrice = Price(supplier = Supplier.SERCO, fromLocation = mock(), toLocation = mock(), effectiveYear = 2020, priceInPence = 1000, addedAt = LocalDate.of(2020, 7, 27).atStartOfDay())
    val adjustedPrice = originalPrice.adjusted(amount = Money(2000), effectiveYear = 2021, addedAt = LocalDate.of(2021, 7, 27).atStartOfDay())

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
}
