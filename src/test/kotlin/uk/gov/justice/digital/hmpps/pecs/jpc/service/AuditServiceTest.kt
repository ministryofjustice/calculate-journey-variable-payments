package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime

internal class AuditableEventMatcher(var auditableEvent: AuditableEvent) : ArgumentMatcher<AuditableEvent> {
  override fun matches(otherAuditableEvent: AuditableEvent): Boolean {
    return auditableEvent.username == otherAuditableEvent.username &&
      auditableEvent.timestamp == auditableEvent.timestamp &&
      auditableEvent.extras == otherAuditableEvent.extras &&
      auditableEvent.type == otherAuditableEvent.type
  }
}

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
  private val service = AuditService(auditEventRepository)
  private val dateTime = LocalDateTime.of(2021, 1, 1, 12, 34, 56)
  private val timeSource = TimeSource { dateTime }
  private val authentication: Authentication = mock()

  private fun verifyEvent(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) =
    verify(auditEventRepository, times(1)).save(
      argThat(
        AuditEventMatcher(
          AuditEvent(
            type,
            dateTime,
            username,
            if (metadata != null) {
              Klaxon().toJsonString(metadata)
            } else {
              null
            }
          )
        )
      )
    )

  @BeforeEach
  private fun initMocks() {
    whenever(authentication.name).thenReturn(" mOcK NAME      ")
  }

  @Test
  internal fun `create log in audit event`() {
    assertThatThrownBy { AuditableEvent.createLogInEvent(timeSource) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOG_IN without a user")

    service.create(AuditableEvent.createLogInEvent(timeSource, authentication))

    verifyEvent(AuditEventType.LOG_IN, "MOCK NAME")
  }

  @Test
  internal fun `create log out audit event`() {
    assertThatThrownBy { AuditableEvent.createLogOutEvent(timeSource) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOG_OUT without a user")

    service.create(AuditableEvent.createLogOutEvent(timeSource, authentication))

    verifyEvent(AuditEventType.LOG_OUT, "MOCK NAME")
  }

  @Test
  internal fun `create download spreadsheet audit event`() {
    assertThatThrownBy {
      AuditableEvent.createDownloadSpreadsheetEvent(
        LocalDate.of(2021, 1, 31),
        "geoamey",
        timeSource
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event DOWNLOAD_SPREADSHEET without a user")

    service.create(
      AuditableEvent.createDownloadSpreadsheetEvent(
        LocalDate.of(2021, 2, 1),
        "serco",
        timeSource,
        authentication
      )
    )

    verifyEvent(AuditEventType.DOWNLOAD_SPREADSHEET, "MOCK NAME", mapOf("month" to "2021-02", "supplier" to "serco"))
  }

  @Test
  internal fun `create new location audit event`() {
    assertThatThrownBy {
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.PR, "TEST1", "TEST 1 NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.AP, "TEST2", "TEST 2 NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomisId" to "TEST2", "name" to "TEST 2 NAME", "type" to "AP")
    )
  }

  @Test
  internal fun `create location name change audit event`() {
    assertThatThrownBy {
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.PR, "TEST1", "TEST 1 NAME"),
        Location(LocationType.PR, "TEST1", "TEST A NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.AP, "TEST2", "TEST 2 NAME"),
        Location(LocationType.AP, "TEST2", "TEST B NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomisId" to "TEST2", "oldName" to "TEST 2 NAME", "newName" to "TEST B NAME")
    )
  }

  @Test
  internal fun `create location type change audit event`() {
    assertThatThrownBy {
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.AP, "TEST1", "TEST 1 NAME"),
        Location(LocationType.CO, "TEST1", "TEST 1 NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.createLocationEvent(
        timeSource,
        Location(LocationType.HP, "TEST2", "TEST 2 NAME"),
        Location(LocationType.PR, "TEST2", "TEST 2 NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomisId" to "TEST2", "oldType" to "HP", "newType" to "PR")
    )
  }

  @Test
  internal fun `create journey price set audit event`() {
    assertThatThrownBy {
      AuditableEvent.createJourneyPriceEvent(
        Supplier.GEOAMEY,
        "TEST1",
        "TEST11",
        effectiveYearForDate(timeSource.date()),
        Money.valueOf(1.23),
        timeSource = timeSource
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event JOURNEY_PRICE without a user")

    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        Money.valueOf(2.34),
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf(
        "supplier" to Supplier.SERCO,
        "fromNomisId" to "TEST2",
        "toNomisId" to "TEST21",
        "effectiveYear" to effectiveYearForDate(timeSource.date()),
        "price" to 2.34
      )
    )
  }

  @Test
  internal fun `create journey price change audit event`() {
    assertThatThrownBy {
      AuditableEvent.createJourneyPriceEvent(
        Supplier.GEOAMEY,
        "TEST1",
        "TEST11",
        effectiveYearForDate(timeSource.date()),
        Money.valueOf(1.23),
        Money.valueOf(12.3),
        timeSource = timeSource,
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event JOURNEY_PRICE without a user")

    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        effectiveYearForDate(timeSource.date()),
        Money.valueOf(2.34),
        Money.valueOf(23.4),
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf(
        "supplier" to Supplier.SERCO,
        "fromNomisId" to "TEST2",
        "toNomisId" to "TEST21",
        "effectiveYear" to effectiveYearForDate(timeSource.date()),
        "oldPrice" to 2.34,
        "newPrice" to 23.4
      )
    )
  }

  @Test
  internal fun `create journey price bulk update audit event`() {
    service.create(AuditableEvent.createJourneyPriceBulkUpdateEvent(Supplier.SERCO, 1.5, timeSource = timeSource))
    service.create(
      AuditableEvent.createJourneyPriceBulkUpdateEvent(
        Supplier.GEOAMEY,
        2.0,
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.SERCO, "multiplier" to 1.5)
    )
    verifyEvent(
      AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
      "MOCK NAME",
      mapOf("supplier" to Supplier.GEOAMEY, "multiplier" to 2.0)
    )
  }
}
