package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
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
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime

internal class AuditableEventMatcher(private var auditableEvent: AuditableEvent) : ArgumentMatcher<AuditableEvent> {
  override fun matches(otherAuditableEvent: AuditableEvent): Boolean {
    return auditableEvent.username == otherAuditableEvent.username &&
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
  private val dateTime = LocalDateTime.of(2021, 1, 1, 12, 34, 56)
  private val timeSource = TimeSource { dateTime }
  private val service = AuditService(auditEventRepository, timeSource)
  private val authentication: Authentication = mock()

  private fun verifyEvent(type: AuditEventType, username: String, metadata: Map<String, Any>? = null) =
    verify(auditEventRepository).save(
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
    assertThatThrownBy {
      AuditableEvent.locationEvent(
        Location(LocationType.PR, "TEST1", "TEST 1 NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.locationEvent(
        Location(LocationType.AP, "TEST2", "TEST 2 NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomis_id" to "TEST2", "name" to "TEST 2 NAME", "type" to "AP")
    )
  }

  @Test
  internal fun `create location name change audit event`() {
    assertThatThrownBy {
      AuditableEvent.locationEvent(
        Location(LocationType.PR, "TEST1", "TEST 1 NAME"),
        Location(LocationType.PR, "TEST1", "TEST A NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.locationEvent(
        Location(LocationType.AP, "TEST2", "TEST 2 NAME"),
        Location(LocationType.AP, "TEST2", "TEST B NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomis_id" to "TEST2", "old_name" to "TEST 2 NAME", "new_name" to "TEST B NAME")
    )
  }

  @Test
  internal fun `create location type change audit event`() {
    assertThatThrownBy {
      AuditableEvent.locationEvent(
        Location(LocationType.AP, "TEST1", "TEST 1 NAME"),
        Location(LocationType.CO, "TEST1", "TEST 1 NAME")
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event LOCATION without a user")

    service.create(
      AuditableEvent.locationEvent(
        Location(LocationType.HP, "TEST2", "TEST 2 NAME"),
        Location(LocationType.PR, "TEST2", "TEST 2 NAME"),
        authentication = authentication
      )!!
    )

    verifyEvent(
      AuditEventType.LOCATION,
      "MOCK NAME",
      mapOf("nomis_id" to "TEST2", "old_type" to "HP", "new_type" to "PR")
    )
  }

  @Test
  internal fun `create journey price set audit event`() {
    assertThatThrownBy {
      AuditableEvent.addPriceEvent(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 234,
          effectiveYear = effectiveYearForDate(timeSource.date())
        )
      )
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event JOURNEY_PRICE without a user")

    service.create(
      AuditableEvent.addPriceEvent(
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
    assertThatThrownBy {
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
    }.isInstanceOf(RuntimeException::class.java)
      .hasMessage("Attempted to create audit event JOURNEY_PRICE without a user")

    service.create(
      AuditableEvent.updatePriceEvent(
        Price(
          supplier = Supplier.SERCO,
          fromLocation = Location(LocationType.CC, "TEST2", "TEST2"),
          toLocation = Location(LocationType.CC, "TEST21", "TEST21"),
          priceInPence = 2340,
          effectiveYear = effectiveYearForDate(timeSource.date())
        ),
        Money(234),
        authentication
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
    service.create(
      AuditableEvent.journeyPriceBulkUpdateEvent(
        Supplier.GEOAMEY,
        2.0,
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
