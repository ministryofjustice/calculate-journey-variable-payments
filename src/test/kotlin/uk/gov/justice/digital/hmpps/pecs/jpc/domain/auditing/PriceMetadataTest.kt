package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.Month

internal class PriceMetadataTest {

  private val fromLocation = Location(LocationType.PR, "FROM_AGENCY_ID", "FROM SITE NAME")
  private val toLocation = Location(LocationType.MC, "TO_AGENCY_ID", "TO SITE NAME")
  private val price = Price(supplier = Supplier.SERCO, fromLocation = fromLocation, toLocation = toLocation, effectiveYear = 2020, priceInPence = 2000)

  @Test
  fun `new price`() {
    val metadata = PriceMetadata.new(price.copy(priceInPence = 100))

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(1.0)
    assertThat(metadata.oldPrice).isNull()
    assertThat(metadata.exceptionDeleted).isNull()
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.isAddException()).isFalse
    assertThat(metadata.key()).isEqualTo("SERCO-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  fun `update price`() {
    val metadata = PriceMetadata.update(Money.valueOf(2.0), price.copy(supplier = Supplier.GEOAMEY, priceInPence = 100))

    assertThat(metadata.supplier).isEqualTo(Supplier.GEOAMEY)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(1.0)
    assertThat(metadata.oldPrice).isEqualTo(2.0)
    assertThat(metadata.exceptionDeleted).isNull()
    assertThat(metadata.isUpdate()).isTrue
    assertThat(metadata.isAdjustment()).isFalse
    assertThat(metadata.isAddException()).isFalse
    assertThat(metadata.key()).isEqualTo("GEOAMEY-FROM_AGENCY_ID-TO_AGENCY_ID")
  }

  @Test
  fun `update price fails if prices the same`() {
    assertThatThrownBy { PriceMetadata.update(Money.valueOf(1.0), price.copy(priceInPence = 100)) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Old price and new price are the same '${Money.valueOf(1.0)}'.")
  }

  @Test
  fun `price adjustment`() {
    val metadata = PriceMetadata.adjustment(price, old = Money(100), multiplier = AdjustmentMultiplier(2.0.toBigDecimal()))

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(20.00)
    assertThat(metadata.oldPrice).isEqualTo(1.0)
    assertThat(metadata.multiplier).isEqualTo(2.0.toBigDecimal())
    assertThat(metadata.exceptionDeleted).isNull()
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.isAddException()).isFalse
    assertThat(metadata.isAdjustment()).isTrue
  }

  @Test
  fun `add price exception`() {
    val metadata = PriceMetadata.exception(price, Month.SEPTEMBER, Money.valueOf(30.00))

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(30.00)
    assertThat(metadata.oldPrice).isEqualTo(20.00)
    assertThat(metadata.multiplier).isNull()
    assertThat(metadata.exceptionDeleted).isFalse
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.isAdjustment()).isFalse
    assertThat(metadata.isAddException()).isTrue
  }

  @Test
  fun `remove price exception`() {
    val metadata = PriceMetadata.removeException(price, Month.SEPTEMBER, Money.valueOf(30.00))

    assertThat(metadata.supplier).isEqualTo(Supplier.SERCO)
    assertThat(metadata.fromNomisId).isEqualTo("FROM_AGENCY_ID")
    assertThat(metadata.toNomisId).isEqualTo("TO_AGENCY_ID")
    assertThat(metadata.effectiveYear).isEqualTo(2020)
    assertThat(metadata.newPrice).isEqualTo(30.00)
    assertThat(metadata.oldPrice).isEqualTo(20.00)
    assertThat(metadata.multiplier).isNull()
    assertThat(metadata.exceptionDeleted).isTrue
    assertThat(metadata.isUpdate()).isFalse
    assertThat(metadata.isAdjustment()).isFalse
    assertThat(metadata.isRemoveException()).isTrue
  }
}
