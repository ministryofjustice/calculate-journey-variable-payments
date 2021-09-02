package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDateTime

internal class PriceHistoryDtoTest {

  @Test
  fun `initial journey pricing via the system`() {
    val datetime = LocalDateTime.now()
    val priceMetadata =
      PriceMetadata(Supplier.GEOAMEY, "from_geo_agency_id", "to_geo_agency_id", 2020, Money(1100).pounds())
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, datetime, "_TERMINAL_", priceMetadata)
    val history = PriceHistoryDto.valueOf(Supplier.GEOAMEY, priceEvent)

    assertThat(history).isEqualTo(
      PriceHistoryDto(
        datetime,
        "Journey priced at £11.00. Effective from 2020 to 2021.",
        "SYSTEM"
      )
    )
  }

  @Test
  fun `initial journey pricing via a user`() {
    val datetime = LocalDateTime.now()
    val priceMetadata =
      PriceMetadata(Supplier.SERCO, "from_serco_agency_id", "to_serco_agency_id", 2021, Money(1450).pounds())
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, datetime, "Jane", priceMetadata)
    val history = PriceHistoryDto.valueOf(Supplier.SERCO, priceEvent)

    assertThat(history).isEqualTo(
      PriceHistoryDto(
        datetime,
        "Journey priced at £14.50. Effective from 2021 to 2022.",
        "Jane"
      )
    )
  }

  @Test
  fun `updated journey pricing via a user`() {
    val datetime = LocalDateTime.now()
    val priceMetadata = PriceMetadata(
      Supplier.GEOAMEY,
      "from_agency_id",
      "to_agency_id",
      2021,
      Money(1450).pounds(),
      Money(1650).pounds()
    )
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, datetime, "Jane", priceMetadata)
    val history = PriceHistoryDto.valueOf(Supplier.GEOAMEY, priceEvent)

    assertThat(history).isEqualTo(
      PriceHistoryDto(
        datetime,
        "Price changed from £16.50 to £14.50. Effective from 2021 to 2022.",
        "Jane"
      )
    )
  }

  @Test
  fun `throws a runtime exception if not correct event type`() {
    assertThatThrownBy {
      PriceHistoryDto.valueOf(
        Supplier.GEOAMEY,
        AuditEvent(
          AuditEventType.LOCATION,
          LocalDateTime.now(),
          "Jane",
          MapLocationMetadata("agency_id", "site name", LocationType.CC)
        )
      )
    }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun `throws a runtime exception if incorrect supplier for history event`() {
    val datetime = LocalDateTime.now()
    val priceMetadata =
      PriceMetadata(Supplier.SERCO, "from_serco_agency_id", "to_serco_agency_id", 2022, Money(1450).pounds())
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, datetime, "Jane", priceMetadata)

    assertThatThrownBy {
      PriceHistoryDto.valueOf(
        Supplier.GEOAMEY,
        priceEvent
      )
    }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun `price change via an a price adjustment`() {
    val datetime = LocalDateTime.now()
    val priceMetadata = PriceMetadata(
      supplier = Supplier.GEOAMEY,
      fromNomisId = "from_agency_id",
      toNomisId = "to_agency_id",
      effectiveYear = 2021,
      oldPrice = Money(1000).pounds(),
      newPrice = Money(2000).pounds(),
      multiplier = 2.0
    )
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, datetime, "Jane", priceMetadata)
    val history = PriceHistoryDto.valueOf(Supplier.GEOAMEY, priceEvent)

    assertThat(history).isEqualTo(
      PriceHistoryDto(
        datetime,
        "Price adjusted from £10.00 to £20.00 with blended rate multiplier 2.0. Effective from 2021 to 2022.",
        "Jane"
      )
    )
  }
}
