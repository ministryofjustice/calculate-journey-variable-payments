package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.MapLocationMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import java.time.LocalDateTime

internal class LocationHistoryDtoTest {

  @Test
  fun `initial mapping of location via the system`() {
    val datetime = LocalDateTime.now()
    val locationMetadata = MapLocationMetadata("agency_id", "Court Name", LocationType.CO)
    val locationEvent = AuditEvent(AuditEventType.LOCATION, datetime, "_TERMINAL_", locationMetadata)

    val history = LocationHistoryDto.valueOf(locationEvent)

    assertThat(history).isEqualTo(LocationHistoryDto(datetime, "Assigned to location name 'Court Name' and type 'County Court'", "SYSTEM"))
  }

  @Test
  fun `initial mapping of location via a user`() {
    val datetime = LocalDateTime.now()
    val locationMetadata = MapLocationMetadata("agency_id", "Probation Name", LocationType.PB)
    val locationEvent = AuditEvent(AuditEventType.LOCATION, datetime, "Bob", locationMetadata)

    val history = LocationHistoryDto.valueOf(locationEvent)

    assertThat(history).isEqualTo(LocationHistoryDto(datetime, "Assigned to location name 'Probation Name' and type 'Probation'", "Bob"))
  }

  @Test
  fun `update mapping of location name only by user`() {
    val datetime = LocalDateTime.now()
    val locationMetadata = MapLocationMetadata("agency_id", "New Probation Name", LocationType.PB, "Probation Name")
    val locationEvent = AuditEvent(AuditEventType.LOCATION, datetime, "Jane", locationMetadata)

    val history = LocationHistoryDto.valueOf(locationEvent)

    assertThat(history).isEqualTo(LocationHistoryDto(datetime, "Location name changed from 'Probation Name' to 'New Probation Name'", "Jane"))
  }

  @Test
  fun `update mapping of location type only by user`() {
    val datetime = LocalDateTime.now()
    val locationMetadata = MapLocationMetadata("agency_id", "Probation Name", LocationType.PB, oldType = LocationType.CO)
    val locationEvent = AuditEvent(AuditEventType.LOCATION, datetime, "Jane", locationMetadata)

    val history = LocationHistoryDto.valueOf(locationEvent)

    assertThat(history).isEqualTo(LocationHistoryDto(datetime, "Location type changed from 'County Court' to 'Probation'", "Jane"))
  }

  @Test
  fun `update mapping of location name ane type by user`() {
    val datetime = LocalDateTime.now()
    val locationMetadata = MapLocationMetadata("agency_id", "New Probation Name", LocationType.PB, "Old Probation Name", LocationType.CO)
    val locationEvent = AuditEvent(AuditEventType.LOCATION, datetime, "Jane", locationMetadata)

    val history = LocationHistoryDto.valueOf(locationEvent)

    assertThat(history).isEqualTo(LocationHistoryDto(datetime, "Location name changed from 'Old Probation Name' to 'New Probation Name' and location type changed from 'County Court' to 'Probation'", "Jane"))
  }

  @Test
  fun `throws a runtime exception if not correct event type`() {
    assertThatThrownBy { LocationHistoryDto.valueOf(AuditEvent(AuditEventType.JOURNEY_PRICE, LocalDateTime.now(), "Jane", metadata = "")) }
  }
}
