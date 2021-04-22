package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
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

internal class AuditServiceTest {
  private val auditEventRepository: AuditEventRepository = mock()
  private val dateTime = LocalDateTime.of(2021, 1, 1, 12, 34, 56)
  private val timeSource = TimeSource { dateTime }
  private val service = AuditService(auditEventRepository, timeSource)
  private val authentication: Authentication = mock { on { name } doReturn " mOcK NAME      " }
  private val securityContext: SecurityContext = mock { on { authentication } doReturn authentication }
  private val eventCaptor = argumentCaptor<AuditEvent>()

  private fun verifyEvent(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) =
    verify(auditEventRepository).save(
      argThat(
        AuditEventMatcher(
          AuditEvent(
            type,
            dateTime,
            username,
            if (metadata != null) Klaxon().toJsonString(metadata) else null
          )
        )
      )
    )

  @BeforeEach
  private fun initMocks() {
    whenever(authentication.name).thenReturn(" mOcK NAME      ")
    SecurityContextHolder.setContext(securityContext)
  }

  @AfterEach
  private fun deInitMocks() {
    SecurityContextHolder.clearContext()
  }

  @Test
  internal fun `create log in audit event`() {
    service.create(AuditableEvent.logInEvent(authentication))

    verifyEvent(AuditEventType.LOG_IN, "MOCK NAME")
  }

  @Test
  internal fun `create log out audit event`() {
    service.create(AuditableEvent.logOutEvent(authentication))

    verifyEvent(AuditEventType.LOG_OUT, "MOCK NAME")
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

    verifyEvent(AuditEventType.DOWNLOAD_SPREADSHEET, "MOCK NAME", mapOf("month" to "2021-02", "supplier" to "SERCO"))
  }

  @Test
  internal fun `create new location audit event`() {
    service.create(AuditableEvent.mapLocation(Location(LocationType.AP, "TEST2", "TEST 2 NAME")))

    verify(auditEventRepository).save(eventCaptor.capture())
    assertThat(eventCaptor.firstValue.eventType).isEqualTo(AuditEventType.LOCATION)
    assertThat(eventCaptor.firstValue.username).isEqualTo("MOCK NAME")
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
    assertThat(eventCaptor.firstValue.username).isEqualTo("MOCK NAME")
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
    assertThat(eventCaptor.firstValue.username).isEqualTo("MOCK NAME")
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
    assertThat(jsonToMapLocation(auditEvent.metadata!!)!!).isEqualTo(mapLocationMetadata)
  }

  private fun jsonToMapLocation(json: String) = Klaxon().parse<MapLocationMetadata>(json)

  @Test
  internal fun `create journey price set audit event`() {
    service.create(
      AuditableEvent.addPriceEvent(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 234,
          effectiveYear = effectiveYearForDate(timeSource.date())
        )
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf(
        "supplier" to Supplier.SERCO,
        "from_nomis_id" to "TEST2",
        "to_nomis_id" to "TEST21",
        "effective_year" to effectiveYearForDate(timeSource.date()),
        "price" to 2.34
      )
    )
  }

  @Test
  internal fun `create journey price change audit event`() {
    service.create(
      AuditableEvent.updatePriceEvent(
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
    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf(
        "supplier" to Supplier.SERCO,
        "from_nomis_id" to "TEST2",
        "to_nomis_id" to "TEST21",
        "effective_year" to effectiveYearForDate(timeSource.date()),
        "old_price" to 2.34,
        "new_price" to 23.4
      )
    )
  }

  @Test
  internal fun `create journey price bulk update audit event`() {
    service.create(AuditableEvent.journeyPriceBulkUpdateEvent(Supplier.SERCO, 1.5))
    service.create(AuditableEvent.journeyPriceBulkUpdateEvent(Supplier.GEOAMEY, 2.0))

    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.SERCO, "multiplier" to 1.5)
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.GEOAMEY, "multiplier" to 2.0)
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
}
