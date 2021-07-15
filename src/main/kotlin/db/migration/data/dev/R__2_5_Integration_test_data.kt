package db.migration.data.dev

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.util.UUID

/**
 * This is a code based migration which serves two (main) purposes:
 *
 * 1. Generate a fresh set of moves on the day of execution to assist with development.
 * 2. As in point one, we can the moves in the integration tests as we have fine control over the move dates.
 *
 */
@Suppress("ClassName", "unused")
class R__2_5_Integration_test_data : BaseJavaMigration() {

  override fun migrate(context: Context) {
    JdbcTemplate(SingleConnectionDataSource(context.connection, true)).also {
      createStandardMoves(it)
    }
  }

  private fun createStandardMoves(template: JdbcTemplate) {
    mapOf(
      "SM1" to "PR1",
      "SM2" to "PR2",
      "SM3" to "PR3"
    ).forEach {
      create(move(moveId = it.key, profileId = it.value, fromAgencyId = "SFROM", toAgencyId = "STO"), template).also { move ->
        create(moveStartEvent(move), template)
        create(moveCompleteEvent(move), template)
        create(journey(move, fromAgencyId = "SFROM", toAgencyId = "STO"), template).also { journey ->
          create(journeyStartEvent(journey), template)
          create(journeyCompleteEvent(journey), template)
        }
      }
    }
  }

  private fun create(move: Move, template: JdbcTemplate): Move {
    template.update(
      moveSql,
      move.dropOffOrCancelledDateTime,
      move.fromNomisAgencyId,
      move.moveDate,
      move.moveId,
      move.moveType!!.name,
      move.notes,
      move.pickUpDateTime,
      move.profileId,
      move.reference,
      move.reportFromLocationType,
      move.reportToLocationType,
      move.status.name,
      move.supplier.name,
      move.toNomisAgencyId,
      move.updatedAt
    )

    return move
  }

  private fun create(journey: Journey, template: JdbcTemplate): Journey {
    template.update(
      journeySql,
      journey.billable,
      journey.clientTimeStamp,
      journey.dropOffDateTime,
      journey.effectiveYear,
      journey.fromNomisAgencyId,
      journey.journeyId,
      journey.moveId,
      journey.notes,
      journey.pickUpDateTime,
      journey.state.name,
      journey.supplier.name,
      journey.toNomisAgencyId,
      journey.updatedAt,
      journey.vehicleRegistration
    )

    return journey
  }

  private fun create(event: Event, template: JdbcTemplate) {
    template.update(
      eventSql,
      event.eventId,
      event.eventableId,
      event.eventableType,
      event.notes,
      event.occurredAt,
      event.recordedAt,
      event.supplier?.name,
      event.type,
      event.updatedAt
    )
  }
}

private val today = LocalDate.now()

private fun move(
  moveId: String,
  type: MoveType = MoveType.STANDARD,
  profileId: String,
  fromAgencyId: String,
  toAgencyId: String,
  supplier: Supplier = Supplier.SERCO
) = Move(
  dropOffOrCancelledDateTime = today.atStartOfDay().plusHours(12),
  fromNomisAgencyId = fromAgencyId,
  moveDate = today,
  moveId = moveId,
  moveType = type,
  notes = "some notes",
  pickUpDateTime = today.atStartOfDay().plusHours(10),
  profileId = profileId,
  reference = "${type.name}$moveId",
  reportFromLocationType = "prison",
  reportToLocationType = "prison",
  status = MoveStatus.completed,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = today.atStartOfDay()
)

private val moveSql = """
  insert into moves
  (
    drop_off_or_cancelled,
    from_nomis_agency_id,
    move_date,
    move_id,
    move_type,
    notes,
    pick_up,
    profile_id,
    reference,
    report_from_location_type,
    report_to_location_type,
    status,
    supplier,
    to_nomis_agency_id,
    updated_at
  ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()

private fun moveStartEvent(move: Move, supplier: Supplier = Supplier.SERCO) = Event(
  details = emptyMap(),
  eventId = "ME" + UUID.randomUUID().toString(),
  eventableId = move.moveId,
  eventableType = "move",
  notes = "Note for move start event",
  occurredAt = move.pickUpDateTime!!,
  recordedAt = today.atStartOfDay(),
  supplier = supplier,
  type = "MoveStart",
  updatedAt = today.atStartOfDay(),
)
private fun moveCompleteEvent(move: Move, supplier: Supplier = Supplier.SERCO) = Event(
  details = emptyMap(),
  eventId = "ME" + UUID.randomUUID().toString(),
  eventableId = move.moveId,
  eventableType = "move",
  notes = "Note for move complete event",
  occurredAt = move.dropOffOrCancelledDateTime!!,
  recordedAt = today.atStartOfDay(),
  supplier = supplier,
  type = "MoveComplete",
  updatedAt = today.atStartOfDay(),
)

private fun journey(
  move: Move,
  fromAgencyId: String,
  toAgencyId: String,
  supplier: Supplier = Supplier.SERCO
) = Journey(
  billable = true,
  clientTimeStamp = today.atStartOfDay(),
  dropOffDateTime = move.dropOffOrCancelledDateTime,
  effectiveYear = effectiveYearForDate(today),
  fromNomisAgencyId = fromAgencyId,
  journeyId = UUID.randomUUID().toString(),
  moveId = move.moveId,
  notes = move.notes,
  pickUpDateTime = move.pickUpDateTime,
  state = JourneyState.completed,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = today.atStartOfDay(),
  vehicleRegistration = "ABCDEFG"
)

private val journeySql = """
  insert into journeys
  (
    billable,
    client_timestamp,
    drop_off,
    effective_year,
    from_nomis_agency_id,
    journey_id,
    move_id,
    notes,
    pick_up,
    state,
    supplier,
    to_nomis_agency_id,
    updated_at,
    vehicle_registration
  ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()

private fun journeyStartEvent(journey: Journey, supplier: Supplier = Supplier.SERCO) = Event(
  details = emptyMap(),
  eventId = "JE" + UUID.randomUUID().toString(),
  eventableId = journey.journeyId,
  eventableType = "journey",
  notes = "Note for journey start event",
  occurredAt = journey.pickUpDateTime!!,
  recordedAt = today.atStartOfDay(),
  supplier = supplier,
  type = "JourneyStart",
  updatedAt = today.atStartOfDay(),
)

private fun journeyCompleteEvent(journey: Journey, supplier: Supplier = Supplier.SERCO) = Event(
  details = emptyMap(),
  eventId = "JE" + UUID.randomUUID().toString(),
  eventableId = journey.journeyId,
  eventableType = "journey",
  notes = "Note for journey complete event",
  occurredAt = journey.dropOffDateTime!!,
  recordedAt = today.atStartOfDay(),
  supplier = supplier,
  type = "JourneyComplete",
  updatedAt = today.atStartOfDay(),
)

private val eventSql = """
  insert into events (
    event_id, 
    eventable_id, 
    eventable_type,
    notes,
    occurred_at,
    recorded_at,
    supplier,
    event_type,
    updated_at
  ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()
