package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime

internal class DatelessAuditableEventMatcher(var auditableEvent: AuditableEvent) : ArgumentMatcher<AuditableEvent> {
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
    service.create(AuditableEvent.createLogInEvent(timeSource))
    service.create(AuditableEvent.createLogInEvent(timeSource, authentication))

    verifyEvent(AuditEventType.LOG_IN, "_TERMINAL_")
    verifyEvent(AuditEventType.LOG_IN, "MOCK NAME")
  }

  @Test
  internal fun `create log out audit event`() {
    service.create(AuditableEvent.createLogOutEvent(timeSource))
    service.create(AuditableEvent.createLogOutEvent(timeSource, authentication))

    verifyEvent(AuditEventType.LOG_OUT, "_TERMINAL_")
    verifyEvent(AuditEventType.LOG_OUT, "MOCK NAME")
  }

  @Test
  internal fun `create download spreadsheet audit event`() {
    service.create(AuditableEvent.createDownloadSpreadsheetEvent(LocalDate.of(2021, 1, 31), "geoamey", timeSource))
    service.create(
      AuditableEvent.createDownloadSpreadsheetEvent(
        LocalDate.of(2021, 2, 1),
        "serco",
        timeSource,
        authentication
      )
    )

    verifyEvent(AuditEventType.DOWNLOAD_SPREADSHEET, "_TERMINAL_", mapOf("month" to "2021-01", "supplier" to "geoamey"))
    verifyEvent(AuditEventType.DOWNLOAD_SPREADSHEET, "MOCK NAME", mapOf("month" to "2021-02", "supplier" to "serco"))
  }

  @Test
  internal fun `create location name set audit event`() {
    service.create(AuditableEvent.createLocationNameEvent("TEST1", "TEST 1 NAME", timeSource = timeSource))
    service.create(
      AuditableEvent.createLocationNameEvent(
        "TEST2",
        "TEST 2 NAME",
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(AuditEventType.LOCATION_NAME, "_TERMINAL_", mapOf("nomisId" to "TEST1", "name" to "TEST 1 NAME"))
    verifyEvent(AuditEventType.LOCATION_NAME, "MOCK NAME", mapOf("nomisId" to "TEST2", "name" to "TEST 2 NAME"))
  }

  @Test
  internal fun `create location name change audit event`() {
    service.create(
      AuditableEvent.createLocationNameEvent(
        "TEST1",
        "TEST 1 NAME",
        "TEST A NAME",
        timeSource = timeSource
      )
    )
    service.create(
      AuditableEvent.createLocationNameEvent(
        "TEST2",
        "TEST 2 NAME",
        "TEST B NAME",
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.LOCATION_NAME,
      "_TERMINAL_",
      mapOf("nomisId" to "TEST1", "oldName" to "TEST 1 NAME", "newName" to "TEST A NAME")
    )
    verifyEvent(
      AuditEventType.LOCATION_NAME,
      "MOCK NAME",
      mapOf("nomisId" to "TEST2", "oldName" to "TEST 2 NAME", "newName" to "TEST B NAME")
    )
  }

  @Test
  internal fun `create location type set audit event`() {
    service.create(AuditableEvent.createLocationTypeEvent("TEST1", LocationType.AP, timeSource = timeSource))
    service.create(
      AuditableEvent.createLocationTypeEvent(
        "TEST2",
        LocationType.HP,
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(AuditEventType.LOCATION_TYPE, "_TERMINAL_", mapOf("nomisId" to "TEST1", "type" to "AP"))
    verifyEvent(AuditEventType.LOCATION_TYPE, "MOCK NAME", mapOf("nomisId" to "TEST2", "type" to "HP"))
  }

  @Test
  internal fun `create location type change audit event`() {
    service.create(
      AuditableEvent.createLocationTypeEvent(
        "TEST1",
        LocationType.AP,
        LocationType.CO,
        timeSource = timeSource
      )
    )
    service.create(
      AuditableEvent.createLocationTypeEvent(
        "TEST2",
        LocationType.HP,
        LocationType.PR,
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.LOCATION_TYPE,
      "_TERMINAL_",
      mapOf("nomisId" to "TEST1", "oldType" to "AP", "newType" to "CO")
    )
    verifyEvent(
      AuditEventType.LOCATION_TYPE,
      "MOCK NAME",
      mapOf("nomisId" to "TEST2", "oldType" to "HP", "newType" to "PR")
    )
  }

  @Test
  internal fun `create journey price set audit event`() {
    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.GEOAMEY,
        "TEST1",
        "TEST11",
        Money.valueOf(1.23),
        timeSource = timeSource
      )
    )
    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        Money.valueOf(2.34),
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "_TERMINAL_",
      mapOf("supplier" to Supplier.GEOAMEY, "fromNomisId" to "TEST1", "toNomisId" to "TEST11", "price" to 1.23)
    )
    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf("supplier" to Supplier.SERCO, "fromNomisId" to "TEST2", "toNomisId" to "TEST21", "price" to 2.34)
    )
  }

  @Test
  internal fun `create journey price change audit event`() {
    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.GEOAMEY,
        "TEST1",
        "TEST11",
        Money.valueOf(1.23),
        Money.valueOf(12.3),
        timeSource = timeSource,
      )
    )
    service.create(
      AuditableEvent.createJourneyPriceEvent(
        Supplier.SERCO,
        "TEST2",
        "TEST21",
        Money.valueOf(2.34),
        Money.valueOf(23.4),
        timeSource = timeSource,
        authentication = authentication
      )
    )

    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "_TERMINAL_",
      mapOf(
        "supplier" to Supplier.GEOAMEY,
        "fromNomisId" to "TEST1",
        "toNomisId" to "TEST11",
        "oldPrice" to 1.23,
        "newPrice" to 12.3
      )
    )
    verifyEvent(
      AuditEventType.JOURNEY_PRICE,
      "MOCK NAME",
      mapOf(
        "supplier" to Supplier.SERCO,
        "fromNomisId" to "TEST2",
        "toNomisId" to "TEST21",
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
