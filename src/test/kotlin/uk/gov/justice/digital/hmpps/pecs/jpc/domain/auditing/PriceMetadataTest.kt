package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

internal class PriceMetadataTest {

  private val fromLocation = Location(LocationType.PR, "FROM_AGENCY_ID", "FROM SITE NAME")
  private val toLocation = Location(LocationType.MC, "TO_AGENCY_ID", "TO SITE NAME")

  @Test
  fun `new price`() {
    val metadata = PriceMetadata.new(
      Price(
        supplier = Supplier.SERCO,
        fromLocation = fromLocation,
        toLocation = toLocation,
        effectiveYear = 2020,
        priceInPence = 100
      )
    )

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(1.0)
    assertThat(metadata.oldPrice).isNull()
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.key()).isEqualTo("SERCO-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  fun `update price`() {
    val metadata = PriceMetadata.update(
      Money.valueOf(2.0),
      Price(
        supplier = Supplier.GEOAMEY,
        fromLocation = fromLocation,
        toLocation = toLocation,
        effectiveYear = 2020,
        priceInPence = 100
      )
    )

    assertThat(metadata.supplier).isEqualTo(Supplier.GEOAMEY)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(1.0)
    assertThat(metadata.oldPrice).isEqualTo(2.0)
    assertThat(metadata.isUpdate()).isTrue
    assertThat(metadata.isAdjustment()).isFalse
    assertThat(metadata.key()).isEqualTo("GEOAMEY-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  fun `update price fails if prices the same`() {
    assertThatThrownBy {
      PriceMetadata.update(
        Money.valueOf(1.0),
        Price(
          supplier = Supplier.SERCO,
          fromLocation = fromLocation,
          toLocation = toLocation,
          effectiveYear = 2020,
          priceInPence = 100
        )
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Old price and new price are the same '${Money.valueOf(1.0)}'.")
  }

  @Test
  fun `price adjustment`() {
    val metadata = PriceMetadata.adjustment(
      new = Price(
        supplier = Supplier.SERCO,
        fromLocation = fromLocation,
        toLocation = toLocation,
        effectiveYear = 2020,
        priceInPence = 200
      ),
      old = Money(100),
      multiplier = 2.0
    )

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(2.0)
    assertThat(metadata.oldPrice).isEqualTo(1.0)
    assertThat(metadata.multiplier).isEqualTo(2.0)
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.isAdjustment()).isTrue
  }
}
