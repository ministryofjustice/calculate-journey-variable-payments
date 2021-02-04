package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.springframework.context.annotation.Bean
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import java.time.LocalDateTime

private class AuditEventMatcher(var auditEvent: AuditEvent) : ArgumentMatcher<AuditEvent> {
  override fun matches(otherAuditEvent: AuditEvent): Boolean {
    return auditEvent.username == otherAuditEvent.username &&
      auditEvent.details == otherAuditEvent.details &&
      auditEvent.eventType == otherAuditEvent.eventType
  }
}

internal class AuditServiceTest {
  private val auditEventRepository: AuditEventRepository = mock()

  @Bean
  private fun timeSource() = TimeSource { LocalDateTime.now() }

  @Test
  internal fun `create log in audit event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(AuditEventType.LOG_IN, LocalDateTime.now(), "TEST USER", "{}")

    service.createLogInEvent("   test user   ")
    service.createLogInEvent("TEST USER")

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create log out audit event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(AuditEventType.LOG_OUT, LocalDateTime.now(), "TEST USER", "{}")

    service.createLogOutEvent("   test user   ")
    service.createLogOutEvent("TEST USER")

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create spreadsheet download audit event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.DOWNLOAD_SPREADSHEET,
      LocalDateTime.now(),
      "TEST USER",
      "{\"month\": \"2021-01\", \"supplier\": \"geoamey\"}"
    )

    service.createDownloadSpreadsheetEvent("   test user   ", "2021-01", "geoamey")
    service.createDownloadSpreadsheetEvent("TEST USER", "2021-01", "geoamey")

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create location name set event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.LOCATION_NAME_SET,
      LocalDateTime.now(),
      "TEST USER",
      "{\"nomisId\": \"TEST1\", \"name\": \"TEST 1 NAME\"}"
    )

    service.createLocationNameSetEvent("   test user   ", "TEST1", "TEST 1 NAME")
    service.createLocationNameSetEvent("TEST USER", "TEST1", "TEST 1 NAME")

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create location name change event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.LOCATION_NAME_CHANGE,
      LocalDateTime.now(),
      "TEST USER",
      "{\"nomisId\": \"TEST1\", \"oldName\": \"TEST 1 NAME\", \"newName\": \"TEST 2 NAME\"}"
    )

    service.createLocationNameChangeEvent("   test user   ", "TEST1", "TEST 1 NAME", "TEST 2 NAME")
    service.createLocationNameChangeEvent("TEST USER", "TEST1", "TEST 1 NAME", "TEST 2 NAME")

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create location type set event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.LOCATION_TYPE_SET,
      LocalDateTime.now(),
      "TEST USER",
      "{\"nomisId\": \"TEST1\", \"type\": \"AP\"}"
    )

    service.createLocationTypeSetEvent("   test user   ", "TEST1", LocationType.AP)
    service.createLocationTypeSetEvent("TEST USER", "TEST1", LocationType.AP)

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create location type change event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.LOCATION_TYPE_CHANGE,
      LocalDateTime.now(),
      "TEST USER",
      "{\"nomisId\": \"TEST1\", \"oldType\": \"AP\", \"newType\": \"CO\"}"
    )

    service.createLocationTypeChangeEvent("   test user   ", "TEST1", LocationType.AP, LocationType.CO)
    service.createLocationTypeChangeEvent("TEST USER", "TEST1", LocationType.AP, LocationType.CO)

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create journey price set event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.JOURNEY_PRICE_SET,
      LocalDateTime.now(),
      "TEST USER",
      "{\"supplier\": \"SUPPLIER\", \"fromNomisId\": \"TEST1\", \"toNomisId\": \"TEST2\", \"price\": 1.23}"
    )

    service.createJourneyPriceSetEvent("   test user   ", "SUPPLIER", "TEST1", "TEST2", Money.valueOf(1.23))
    service.createJourneyPriceSetEvent("TEST USER", "SUPPLIER", "TEST1", "TEST2", Money.valueOf(1.23))

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }

  @Test
  internal fun `create journey price change event`() {
    val service = AuditService(auditEventRepository)
    ReflectionTestUtils.setField(service, "timeSource", timeSource())

    val expectedAuditEvent = AuditEvent(
      AuditEventType.JOURNEY_PRICE_CHANGE,
      LocalDateTime.now(),
      "TEST USER",
      "{\"supplier\": \"SUPPLIER\", \"fromNomisId\": \"TEST1\", \"toNomisId\": \"TEST2\", \"oldPrice\": 1.23, \"newPrice\": 2.34}"
    )

    service.createJourneyPriceChangeEvent(
      "   test user   ",
      "SUPPLIER",
      "TEST1",
      "TEST2",
      Money.valueOf(1.23),
      Money.valueOf(2.34)
    )
    service.createJourneyPriceChangeEvent(
      "TEST USER",
      "SUPPLIER",
      "TEST1",
      "TEST2",
      Money.valueOf(1.23),
      Money.valueOf(2.34)
    )

    verify(auditEventRepository, times(2)).save(argThat(AuditEventMatcher(expectedAuditEvent)))
  }
}
