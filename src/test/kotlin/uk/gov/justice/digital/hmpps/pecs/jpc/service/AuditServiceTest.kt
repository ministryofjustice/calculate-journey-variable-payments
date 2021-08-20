package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatcher
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime

private class AuditEventMatcher(var auditEvent: AuditEvent) : ArgumentMatcher<AuditEvent> {
  override fun matches(otherAuditEvent: AuditEvent): Boolean {
    return auditEvent.username == otherAuditEvent.username &&
      auditEvent.createdAt == otherAuditEvent.createdAt &&
      auditEvent.metadata == otherAuditEvent.metadata &&
      auditEvent.eventType == otherAuditEvent.eventType
  }
}

@ExtendWith(FakeAuthentication::class)
internal class AuditServiceTest {
  private val auditEventRepository: AuditEventRepository = mock()
  private val dateTime = LocalDateTime.of(2021, 1, 1, 12, 34, 56)
  private val timeSource = TimeSource { dateTime }
  private val service = AuditService(auditEventRepository, timeSource)
  private lateinit var authentication: Authentication
  private val eventCaptor = argumentCaptor<AuditEvent>()

  private fun verifyEvent(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) =
    verify(auditEventRepository).save(
      argThat(
        AuditEventMatcher(
          AuditEvent(
            type,
            dateTime,
            username.trim().uppercase(),
            if (metadata != null) Klaxon().toJsonString(metadata) else null
          )
        )
      )
    )

  @BeforeEach
  fun before() {
    authentication = SecurityContextHolder.getContext().authentication
  }

  @Test
  internal fun `create log in audit event`() {
    service.create(AuditableEvent.logInEvent(authentication))

    verifyEvent(AuditEventType.LOG_IN, authentication.name)
  }

  @Test
  internal fun `create log out audit event`() {
    service.create(AuditableEvent.logOutEvent(authentication))

    verifyEvent(AuditEventType.LOG_OUT, authentication.name)
  }

  @Test
  internal fun `create download spreadsheet audit event`() {
    service.create(
      AuditableEvent.downloadSpreadsheetEvent(
        LocalDate.of(2021, 2, 1),
        Supplier.SERCO,
        authentication
      )
    )

    verifyEvent(
      AuditEventType.DOWNLOAD_SPREADSHEET,
      authentication.name,
      mapOf("month" to "2021-02", "supplier" to "SERCO")
    )
  }

  @Test
  internal fun `create download spreadsheet failure audit event`() {
    service.create(
      AuditableEvent.downloadSpreadsheetFailure(
        LocalDate.of(2021, 6, 7),
        Supplier.GEOAMEY,
        authentication
      )
    )

    verifyEvent(
      AuditEventType.DOWNLOAD_SPREADSHEET_FAILURE,
      authentication.name,
      mapOf("month" to "2021-06", "supplier" to "GEOAMEY")
    )
  }

  @Test
  internal fun `create new location audit event`() {
    service.create(AuditableEvent.mapLocation(Location(LocationType.AP, "TEST2", "TEST 2 NAME")))

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.LOCATION)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())
    assertThatMappedLocationsAreTheSame(
      eventCaptor.firstValue,
      MapLocationMetadata(nomisId = "TEST2", newName = "TEST 2 NAME", newType = LocationType.AP)
    )
  }

  @Test
  internal fun `create location name change audit event`() {
    val location = Location(LocationType.AP, "TEST2", "TEST 2 NAME")

    service.create(
      AuditableEvent.remapLocation(
        location,
        location.copy(siteName = "TEST B NAME")
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.LOCATION)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())
    assertThatMappedLocationsAreTheSame(
      eventCaptor.firstValue,
      MapLocationMetadata(
        nomisId = "TEST2",
        newName = "TEST B NAME",
        oldName = "TEST 2 NAME"
      )
    )
  }

  @Test
  internal fun `create location type change audit event`() {
    val location = Location(LocationType.HP, "TEST2", "TEST 2 NAME")

    service.create(
      AuditableEvent.remapLocation(
        location,
        location.copy(locationType = LocationType.PR)
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.LOCATION)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())
    assertThatMappedLocationsAreTheSame(
      eventCaptor.firstValue,
      MapLocationMetadata(
        nomisId = "TEST2",
        oldType = LocationType.HP,
        newType = LocationType.PR
      )
    )
  }

  private fun assertThatMappedLocationsAreTheSame(auditEvent: AuditEvent, mapLocationMetadata: MapLocationMetadata) {
    assertThat(jsonTo<MapLocationMetadata>(auditEvent.metadata!!)).isEqualTo(mapLocationMetadata)
  }

  @Test
  internal fun `create automatic map location type change audit event`() {
    val location = Location(LocationType.PR, "PRISON_ONE", "PRISON ONE")

    service.create(
      AuditableEvent.autoMapLocation(location)
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.LOCATION)
    assertThat(eventCaptor.firstValue.username).isEqualTo("_TERMINAL_")
    assertThatMappedLocationsAreTheSame(
      eventCaptor.firstValue,
      MapLocationMetadata(nomisId = "PRISON_ONE", newType = LocationType.PR, newName = "PRISON ONE")
    )
  }

  @Test
  internal fun `create authenticated journey price set audit event`() {
    service.create(
      AuditableEvent.addPrice(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 234,
          effectiveYear = effectiveYearForDate(timeSource.date())
        ),
        authentication
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())

    assertThatPricesMetadataIsTheSame(
      eventCaptor.firstValue,
      PriceMetadata(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        2.34,
      )
    )
  }

  @Test
  internal fun `create un-authenticated journey price set audit event`() {
    service.create(
      AuditableEvent.addPrice(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 234,
          effectiveYear = effectiveYearForDate(timeSource.date())
        )
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
    assertThat(eventCaptor.firstValue.username).isEqualTo("_TERMINAL_")

    assertThatPricesMetadataIsTheSame(
      eventCaptor.firstValue,
      PriceMetadata(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        2.34,
      )
    )
  }

  @Test
  internal fun `create journey price change audit event`() {
    service.create(
      AuditableEvent.updatePrice(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 2340,
          effectiveYear = effectiveYearForDate(timeSource.date())
        ),
        Money(234)
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())
    assertThatPricesMetadataIsTheSame(
      eventCaptor.firstValue,
      PriceMetadata(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        23.4,
        2.34
      )
    )
  }

  private fun assertThatPricesMetadataIsTheSame(auditEvent: AuditEvent, priceMetadata: PriceMetadata) {
    assertThat(jsonTo<PriceMetadata>(auditEvent.metadata!!)).isEqualTo(priceMetadata)
  }

  private inline fun <reified T> jsonTo(json: String): T = Klaxon().parse<T>(json)!!

  @Test
  internal fun `create journey price bulk adjustment audit event`() {
    service.create(AuditableEvent.journeyPriceBulkPriceAdjustmentEvent(Supplier.SERCO, 2020, 1.5))
    service.create(AuditableEvent.journeyPriceBulkPriceAdjustmentEvent(Supplier.GEOAMEY, 2021, 2.0))

    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.SERCO, "effective_year" to 2020, "multiplier" to 1.5)
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_ADJUSTMENT,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.GEOAMEY, "effective_year" to 2021, "multiplier" to 2.0)
    )
  }

  @Test
  internal fun `create import reports audit event`() {
    service.create(AuditableEvent.importReportEvent("moves", LocalDate.of(2021, 2, 22), 20, 10))

    verifyEvent(
      AuditEventType.REPORTING_DATA_IMPORT,
      "_TERMINAL_",
      mapOf("type" to "moves", "report_date" to "2021-02-22", "processed" to 20, "saved" to 10)
    )
  }

  @Test
  internal fun `create authenticated journey price adjustment audit event`() {
    service.create(
      AuditableEvent.adjustPrice(
        price = Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 200,
          effectiveYear = effectiveYearForDate(timeSource.date())
        ),
        original = Money(100),
        multiplier = 2.0,
        authentication = authentication
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
    assertThat(eventCaptor.firstValue.username).isEqualTo(authentication.name.trim().uppercase())
    assertThatPricesMetadataIsTheSame(
      eventCaptor.firstValue,
      PriceMetadata(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        2.0,
        1.0,
        2.0
      )
    )
  }

  @Test
  internal fun `create un-authenticated journey price adjustment audit event`() {
    service.create(
      AuditableEvent.adjustPrice(
        price = Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 200,
          effectiveYear = effectiveYearForDate(timeSource.date())
        ),
        original = Money(100),
        multiplier = 2.0,
      )
    )

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
    assertThat(eventCaptor.firstValue.username).isEqualTo("_TERMINAL_")
    assertThatPricesMetadataIsTheSame(
      eventCaptor.firstValue,
      PriceMetadata(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        2.0,
        1.0,
        2.0
      )
    )
  }
}
