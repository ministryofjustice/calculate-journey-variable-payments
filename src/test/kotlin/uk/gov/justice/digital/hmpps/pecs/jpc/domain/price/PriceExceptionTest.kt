package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import java.time.DateTimeException

internal class PriceExceptionTest {

  @Test
  fun `price exception amount must be greater than zero`() {
    assertDoesNotThrow { PriceException(price = mock(), month = 1, priceInPence = 1) }

    assertThatThrownBy { PriceException(price = mock(), month = 1, priceInPence = 0) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
    assertThatThrownBy { PriceException(price = mock(), month = 1, priceInPence = -1) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
  }

  @Test
  fun `price exception allowed month values`() {
    for (month in 1..12) {
      assertDoesNotThrow { PriceException(price = mock(), month = month, priceInPence = 1) }
    }

    assertThatThrownBy {
      PriceException(
        price = mock(),
        month = 0,
        priceInPence = 1,
      )
    }.isInstanceOf(DateTimeException::class.java)
    assertThatThrownBy {
      PriceException(
        price = mock(),
        month = 13,
        priceInPence = 1,
      )
    }.isInstanceOf(DateTimeException::class.java)
  }
}
