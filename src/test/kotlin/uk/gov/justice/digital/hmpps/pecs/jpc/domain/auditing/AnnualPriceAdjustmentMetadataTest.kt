package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDateTime

internal class AnnualPriceAdjustmentMetadataTest {

  @Test
  internal fun `can map Serco bulk price adjustment audit event`() {
    val metadata = AnnualPriceAdjustmentMetadata.map(
      AuditEvent(
        AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
        LocalDateTime.now(),
        "username",
        AnnualPriceAdjustmentMetadata(Supplier.SERCO, 2019, 1.5.toBigDecimal(), "serco details")
      )
    )

    with(metadata) {
      assertThat(supplier).isEqualTo(Supplier.SERCO)
      assertThat(effectiveYear).isEqualTo(2019)
      assertThat(multiplier).isEqualTo(1.5.toBigDecimal())
      assertThat(details).isEqualTo("serco details")
    }
  }

  @Test
  internal fun `can map GEOAmey bulk price adjustment audit event`() {
    val metadata = AnnualPriceAdjustmentMetadata.map(
      AuditEvent(
        AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
        LocalDateTime.now(),
        "username",
        AnnualPriceAdjustmentMetadata(Supplier.GEOAMEY, 2020, 2.0.toBigDecimal(), "geo details")
      )
    )

    with(metadata) {
      assertThat(supplier).isEqualTo(Supplier.GEOAMEY)
      assertThat(effectiveYear).isEqualTo(2020)
      assertThat(multiplier).isEqualTo(2.0.toBigDecimal())
      assertThat(details).isEqualTo("geo details")
    }
  }

  @Test
  internal fun `fails to map Serco bulk price adjustment audit event if wrong event type`() {
    assertThatThrownBy {
      AnnualPriceAdjustmentMetadata.map(
        AuditEvent(
          AuditEventType.JOURNEY_PRICE,
          LocalDateTime.now(),
          "username",
          AnnualPriceAdjustmentMetadata(Supplier.SERCO, 2019, 1.5.toBigDecimal(), "serco details")
        )
      )
    }.isInstanceOf(IllegalArgumentException::class.java)
  }
}
