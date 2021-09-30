package db.migration.data.dev

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * This is a code based migration which serves two (main) purposes:
 *
 * 1. Generate a fresh set of Serco moves starting at the beginning of the previous month. Move data is always in the
 *    past and for purposes of testing all moves need to start and end within the same month.
 * 2. As in point one, we use the moves in the integration tests as we have fine control over the move dates.
 *
 */
@Suppress("ClassName", "unused")
class R__2_5_Integration_test_data : BaseJavaMigration() {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun migrate(context: Context) {
    JdbcTemplate(SingleConnectionDataSource(context.connection, true)).also {
      createStandardMoves(it)
      createRedirectMoves(it)
      createLongHaulMoves(it)
      createLockoutMoves(it)
      createMultiTypeMoves(it)
      createCancelledMoves(it)
    }
  }

  private fun createStandardMoves(template: JdbcTemplate) {
    logger.info("create standard moves")

    fun createMoveEventsAndJourney(move: Move) {
      create(moveStartEvent(move), template)
      create(moveCompleteEvent(move), template)
      create(journey(move, fromAgencyId = "PRISON1", toAgencyId = "PRISON2"), template).also { journey ->
        create(journeyStartEvent(journey), template)
        create(journeyCompleteEvent(journey), template)
      }
    }

    mapOf(
      "SM1" to "PR1",
      "SM2" to "PR2",
      "SM3" to "PR3"
    ).forEach {
      create(
        move(moveId = it.key, profileId = it.value, fromAgencyId = "PRISON1", toAgencyId = "PRISON2"),
        template
      ).also { move -> createMoveEventsAndJourney(move) }
    }

    create(
      move(moveId = "SM4", profileId = "PR1", fromAgencyId = "PRISON1", toAgencyId = "PRISON2", date = startOfPreviousMonth.minusMonths(1)),
      template
    ).also { move -> createMoveEventsAndJourney(move) }
  }

  // TODO need to add the appropriate journey events for the following move types...

  private fun createRedirectMoves(template: JdbcTemplate) {
    logger.info("create redirect moves")

    mapOf(
      "RM1" to "PR4",
    ).forEach {
      create(
        move(
          moveId = it.key,
          profileId = it.value,
          fromAgencyId = "FROM_AGENCY",
          toAgencyId = "TO_AGENCY",
          type = MoveType.REDIRECTION
        ),
        template
      ).also { move ->
        create(moveStartEvent(move), template)
        create(moveRedirectEvent(move), template)
        create(moveCompleteEvent(move), template)
        create(
          journey(
            move,
            fromAgencyId = "FROM_AGENCY",
            toAgencyId = "STOPOVER_AGENCY",
            state = JourneyState.cancelled,
            dropOff = null
          ),
          template
        )
        create(journey(move, fromAgencyId = "STOPOVER_AGENCY", toAgencyId = "TO_AGENCY"), template)
      }
    }
  }

  private fun createLongHaulMoves(template: JdbcTemplate) {
    logger.info("create long haul moves")

    mapOf(
      "LHM1" to "PR5",
    ).forEach {
      create(
        move(
          moveId = it.key,
          profileId = it.value,
          fromAgencyId = "FROM_AGENCY",
          toAgencyId = "TO_AGENCY",
          type = MoveType.LONG_HAUL,
          days = 1
        ),
        template
      ).also { move ->
        create(moveStartEvent(move), template)
        create(moveCompleteEvent(move), template)
        create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "STOPOVER_AGENCY"), template)
        create(
          journey(
            move,
            fromAgencyId = "STOPOVER_AGENCY",
            toAgencyId = "TO_AGENCY",
            pickUp = move.pickUpDateTime?.plusDays(1),
            dropOff = move.dropOffOrCancelledDateTime
          ),
          template
        )
      }
    }
  }

  private fun createLockoutMoves(template: JdbcTemplate) {
    logger.info("create lockout moves")

    mapOf(
      "LM1" to "PR6",
    ).forEach {
      create(
        move(
          moveId = it.key,
          profileId = it.value,
          fromAgencyId = "FROM_AGENCY",
          toAgencyId = "TO_AGENCY",
          type = MoveType.LOCKOUT,
          days = 1
        ),
        template
      ).also { move ->
        create(moveStartEvent(move), template)
        create(moveCompleteEvent(move), template)
        create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "LOCKOUT_AGENCY"), template)
        create(
          journey(
            move,
            fromAgencyId = "LOCKOUT_AGENCY",
            toAgencyId = "TO_AGENCY",
            pickUp = move.pickUpDateTime?.plusDays(1),
            dropOff = move.dropOffOrCancelledDateTime
          ),
          template
        )
      }
    }
  }

  private fun createMultiTypeMoves(template: JdbcTemplate) {
    logger.info("create multi type moves")

    mapOf(
      "MM1" to "PR7",
    ).forEach {
      create(
        move(
          moveId = it.key,
          profileId = it.value,
          fromAgencyId = "FROM_AGENCY",
          toAgencyId = "TO_AGENCY4",
          type = MoveType.MULTI,
          days = 1
        ),
        template
      ).also { move ->
        create(moveStartEvent(move), template)
        create(moveCompleteEvent(move), template)
        create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "TO_AGENCY2"), template)
        create(
          journey(
            move,
            fromAgencyId = "FROM_AGENCY2",
            toAgencyId = "TO_AGENCY3",
            state = JourneyState.cancelled,
            dropOff = null
          ),
          template
        )
        create(
          journey(
            move,
            fromAgencyId = "FROM_AGENCY2",
            toAgencyId = "TO_AGENCY4",
            pickUp = move.pickUpDateTime?.plusDays(1),
            dropOff = move.dropOffOrCancelledDateTime
          ),
          template
        )
      }
    }
  }

  private fun createCancelledMoves(template: JdbcTemplate) {
    logger.info("create cancelled moves")

    mapOf(
      "CM1" to "PR8",
    ).forEach {
      create(
        move(
          moveId = it.key,
          profileId = it.value,
          fromAgencyId = "FROM_AGENCY",
          toAgencyId = "TO_AGENCY",
          type = MoveType.CANCELLED
        ),
        template
      ).also { move ->
        create(moveAcceptEvent(move), template)
        create(moveCancelEvent(move), template)
        create(
          journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "TO_AGENCY", state = JourneyState.cancelled),
          template
        )
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

private val startOfPreviousMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1)

private fun move(
  moveId: String,
  type: MoveType = MoveType.STANDARD,
  profileId: String,
  fromAgencyId: String,
  toAgencyId: String,
  supplier: Supplier = Supplier.SERCO,
  days: Long = 0,
  date: LocalDate = startOfPreviousMonth
) = Move(
  dropOffOrCancelledDateTime = date.plusDays(days).atStartOfDay().plusHours(12),
  fromNomisAgencyId = fromAgencyId,
  moveDate = date,
  moveId = moveId,
  moveType = type,
  notes = "some notes",
  pickUpDateTime = date.atStartOfDay().plusHours(10),
  profileId = profileId,
  reference = "${type.name}$moveId",
  reportFromLocationType = "prison",
  reportToLocationType = "prison",
  status = MoveStatus.completed,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = date.atStartOfDay()
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

private fun moveStartEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Start")

private fun moveRedirectEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Redirect")

private fun moveCompleteEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Complete", move.dropOffOrCancelledDateTime)

private fun moveAcceptEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Accept")

private fun moveCancelEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Cancel")

private fun moveEvent(move: Move, supplier: Supplier = Supplier.SERCO, type: String, occurredAt: LocalDateTime? = null) = Event(
  details = emptyMap(),
  eventId = "ME" + UUID.randomUUID().toString(),
  eventableId = move.moveId,
  eventableType = "move",
  notes = "Note for move $type event",
  occurredAt = occurredAt ?: move.pickUpDateTime!!,
  recordedAt = startOfPreviousMonth.atStartOfDay(),
  supplier = supplier,
  type = "Move$type",
  updatedAt = startOfPreviousMonth.atStartOfDay(),
)

private fun journey(
  move: Move,
  fromAgencyId: String,
  toAgencyId: String,
  supplier: Supplier = Supplier.SERCO,
  state: JourneyState = JourneyState.completed,
  pickUp: LocalDateTime? = move.pickUpDateTime,
  dropOff: LocalDateTime? = move.dropOffOrCancelledDateTime
) = Journey(
  billable = true,
  clientTimeStamp = startOfPreviousMonth.atStartOfDay(),
  dropOffDateTime = dropOff,
  effectiveYear = effectiveYearForDate(startOfPreviousMonth),
  fromNomisAgencyId = fromAgencyId,
  journeyId = UUID.randomUUID().toString(),
  moveId = move.moveId,
  notes = move.notes,
  pickUpDateTime = pickUp,
  state = state,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = startOfPreviousMonth.atStartOfDay(),
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
  recordedAt = startOfPreviousMonth.atStartOfDay(),
  supplier = supplier,
  type = "JourneyStart",
  updatedAt = startOfPreviousMonth.atStartOfDay(),
)

private fun journeyCompleteEvent(journey: Journey, supplier: Supplier = Supplier.SERCO) = Event(
  details = emptyMap(),
  eventId = "JE" + UUID.randomUUID().toString(),
  eventableId = journey.journeyId,
  eventableType = "journey",
  notes = "Note for journey complete event",
  occurredAt = journey.dropOffDateTime!!,
  recordedAt = startOfPreviousMonth.atStartOfDay(),
  supplier = supplier,
  type = "JourneyComplete",
  updatedAt = startOfPreviousMonth.atStartOfDay(),
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
