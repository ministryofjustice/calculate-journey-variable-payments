package db.migration.data.dev

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.JpaDetailsConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
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
 * Note: no audit events are created as part of this migration.
 *
 */
@Suppress("ClassName", "unused")
class R__2_5_Integration_test_data : BaseJavaMigration() {

  private val logger = LoggerFactory.getLogger(javaClass)

  companion object {
    val PRISON1_PRIMARY_KEY: UUID = UUID.fromString("709fbee3-7fe6-4584-a8dc-f12481165bfa")
    val PRISON2_PRIMARY_KEY: UUID = UUID.fromString("612ec4d3-9cfa-4c89-ad39-3f02acc8b41d")
    val POLICE1_PRIMARY_KEY: UUID = UUID.fromString("13c46837-c5c9-45a4-83d5-5a0d1438ff3c")
  }

  private data class Priced(
    val fromPrimaryKey: UUID,
    val fromAgencyId: String,
    val toPrimaryKey: UUID,
    val toAgencyId: String,
    val amount: Money,
  )

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

    fun createMoveEventsAndJourney(move: Move, priced: Priced? = null): Move {
      val moveEvents = listOf(create(moveStartEvent(move), template), create(moveCompleteEvent(move), template))

      val journey = create(
        journey(move, fromAgencyId = priced?.fromAgencyId ?: "PRISON1", priced?.toAgencyId ?: "PRISON2"),
        template,
      ).let {
        it.copy(
          events = listOf(
            create(journeyStartEvent(it, details = mapOf("vehicle_reg" to "ABDCEFG")), template),
            create(journeyCompleteEvent(it, details = mapOf("vehicle_reg" to "HIJKLMN")), template),
          ),
        )
      }

      if (priced != null) {
        template.update(
          priceSql,
          UUID.randomUUID(),
          LocalDateTime.now(),
          journey.effectiveYear,
          priced.amount.pence,
          Supplier.SERCO.name,
          priced.fromPrimaryKey,
          priced.toPrimaryKey,
        )
      }

      return move.copy(events = moveEvents, journeys = listOf(journey))
    }

    fun createUnpricedStandardMovesInsideTwoYearChangeWindow() {
      mapOf(
        "SM1" to "PR1",
        "SM2" to "PR2",
        "SM3" to "PR3",
      ).forEach {
        create(
          move(moveId = it.key, profileId = it.value, fromAgencyId = "PRISON1", toAgencyId = "PRISON2"),
          template,
        ).also { move ->
          createMoveEventsAndJourney(move).failMigrationIfNotMoveType(MoveType.STANDARD)
        }
      }
    }

    fun createPricedStandardMoveInsideTwoYearChangeWindow() {
      create(
        move(
          moveId = "SM4",
          profileId = "PR1",
          fromAgencyId = "PRISON1",
          toAgencyId = "POLICE1",
          date = startOfPreviousMonth.minusMonths(1),
        ),
        template,
      ).also { move ->
        createMoveEventsAndJourney(
          move,
          Priced(
            fromPrimaryKey = PRISON1_PRIMARY_KEY,
            fromAgencyId = "PRISON1",
            toPrimaryKey = POLICE1_PRIMARY_KEY,
            toAgencyId = "POLICE1",
            amount = Money.valueOf("100.00"),
          ),
        ).failMigrationIfNotMoveType(MoveType.STANDARD)
      }
    }

    fun createUnpricedStandardMoveOutsideTwoYearChangeWindow() {
      create(
        move(
          moveId = "SM5",
          profileId = "PR2",
          fromAgencyId = "PRISON1",
          toAgencyId = "PRISON2",
          date = startOfPreviousMonth.minusYears(2),
        ),
        template,
      ).also {
        createMoveEventsAndJourney(it).failMigrationIfNotMoveType(MoveType.STANDARD)
      }
    }

    fun createPricedStandardMoveOutsideTwoYearChangeWindow() {
      create(
        move(
          moveId = "SM6",
          profileId = "PR1",
          fromAgencyId = "PRISON1",
          toAgencyId = "POLICE1",
          date = startOfPreviousMonth.minusYears(2),
        ),
        template,
      ).also { move ->
        createMoveEventsAndJourney(
          move,
          Priced(
            fromPrimaryKey = PRISON1_PRIMARY_KEY,
            fromAgencyId = "PRISON1",
            toPrimaryKey = POLICE1_PRIMARY_KEY,
            toAgencyId = "POLICE1",
            amount = Money.valueOf("50.00"),
          ),
        ).failMigrationIfNotMoveType(MoveType.STANDARD)
      }
    }

    createUnpricedStandardMovesInsideTwoYearChangeWindow()
    createPricedStandardMoveInsideTwoYearChangeWindow()
    createUnpricedStandardMoveOutsideTwoYearChangeWindow()
    createPricedStandardMoveOutsideTwoYearChangeWindow()
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
          type = MoveType.REDIRECTION,
        ),
        template,
      ).also { move ->
        move.copy(
          events = listOf(
            create(moveStartEvent(move), template),
            create(moveRedirectEvent(move), template),
            create(moveCompleteEvent(move), template),
          ),
          journeys = listOf(
            create(
              journey(
                move,
                fromAgencyId = "FROM_AGENCY",
                toAgencyId = "STOPOVER_AGENCY",
                state = JourneyState.cancelled,
                dropOff = null,
              ),
              template,
            ),
            create(journey(move, fromAgencyId = "STOPOVER_AGENCY", toAgencyId = "TO_AGENCY"), template),
          ),
        ).failMigrationIfNotMoveType(MoveType.REDIRECTION)
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
          days = 1,
        ),
        template,
      ).also { move ->
        move.copy(
          events = listOf(
            create(moveStartEvent(move), template),
            create(moveCompleteEvent(move), template),
            create(moveLodgingStartEvent(move), template),
            create(moveLodgingEndEvent(move), template),
          ),
          journeys = listOf(
            create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "STOPOVER_AGENCY"), template),
            create(
              journey(
                move,
                fromAgencyId = "STOPOVER_AGENCY",
                toAgencyId = "TO_AGENCY",
                pickUp = move.pickUpDateTime?.plusDays(1),
                dropOff = move.dropOffOrCancelledDateTime,
              ),
              template,
            ),
          ),
        ).failMigrationIfNotMoveType(MoveType.LONG_HAUL)
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
          days = 1,
        ),
        template,
      ).also { move ->
        move.copy(
          events = listOf(
            create(moveStartEvent(move), template),
            create(moveCompleteEvent(move), template),
            create(moveLockoutEvent(move), template),
          ),
          journeys = listOf(
            create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "LOCKOUT_AGENCY"), template),
            create(
              journey(
                move,
                fromAgencyId = "LOCKOUT_AGENCY",
                toAgencyId = "TO_AGENCY",
                pickUp = move.pickUpDateTime?.plusDays(1),
                dropOff = move.dropOffOrCancelledDateTime,
              ),
              template,
            ),
          ),
        ).failMigrationIfNotMoveType(MoveType.LOCKOUT)
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
          days = 1,
        ),
        template,
      ).also { move ->
        move.copy(
          events = listOf(
            create(moveStartEvent(move), template),
            create(moveCompleteEvent(move), template),
          ),
          journeys = listOf(
            create(journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "TO_AGENCY2"), template),
            create(
              journey(
                move,
                fromAgencyId = "FROM_AGENCY2",
                toAgencyId = "TO_AGENCY3",
                state = JourneyState.cancelled,
                dropOff = null,
              ),
              template,
            ),
            create(
              journey(
                move,
                fromAgencyId = "FROM_AGENCY2",
                toAgencyId = "TO_AGENCY4",
                pickUp = move.pickUpDateTime?.plusDays(1),
                dropOff = move.dropOffOrCancelledDateTime,
              ),
              template,
            ),
          ),
        ).failMigrationIfNotMoveType(MoveType.MULTI)
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
          type = MoveType.CANCELLED,
          cancellationReason = Move.CANCELLATION_REASON_CANCELLED_BY_PMU,
          status = MoveStatus.cancelled,
        ),
        template,
      ).also { move ->
        move.copy(
          events = listOf(
            create(moveAcceptEvent(move), template),
            create(moveCancelEvent(move), template),
          ),
          journeys = listOf(
            create(
              journey(move, fromAgencyId = "FROM_AGENCY", toAgencyId = "TO_AGENCY", state = JourneyState.cancelled),
              template,
            ),
          ),
        ).failMigrationIfNotMoveType(MoveType.CANCELLED)
      }
    }
  }

  private fun Move.failMigrationIfNotMoveType(moveType: MoveType) {
    if (moveType.hasMoveType(this).not()) {
      logger.error("Move should be a $moveType but is not $this")
      throw RuntimeException("Migration failed as move ${this.moveId} is not expected move type $moveType")
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
      move.updatedAt,
      move.cancellationReason,
      move.moveMonth,
      move.moveYear,
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
      journey.vehicleRegistrations(),
    )

    return journey
  }

  private fun create(event: Event, template: JdbcTemplate): Event {
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
      event.updatedAt,
      event.details?.let { JpaDetailsConverter().convertToDatabaseColumn(it) },
    )

    return event
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
  date: LocalDate = startOfPreviousMonth,
  cancellationReason: String? = null,
  status: MoveStatus = MoveStatus.completed,
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
  status = status,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = date.atStartOfDay(),
  cancellationReason = cancellationReason,
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
    updated_at,
    cancellation_reason,
    move_month,
    move_year
  ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()

private fun moveStartEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Start")

private fun moveRedirectEvent(move: Move, supplier: Supplier = Supplier.SERCO) =
  moveEvent(move, supplier, "Redirect", move.pickUpDateTime?.plusMinutes(1))

private fun moveCompleteEvent(move: Move, supplier: Supplier = Supplier.SERCO) =
  moveEvent(move, supplier, "Complete", move.dropOffOrCancelledDateTime)

private fun moveAcceptEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Accept")

private fun moveCancelEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Cancel")

private fun moveLodgingStartEvent(move: Move, supplier: Supplier = Supplier.SERCO) =
  moveEvent(move, supplier, "LodgingStart")

private fun moveLodgingEndEvent(move: Move, supplier: Supplier = Supplier.SERCO) =
  moveEvent(move, supplier, "LodgingEnd")

private fun moveLockoutEvent(move: Move, supplier: Supplier = Supplier.SERCO) = moveEvent(move, supplier, "Lockout")

private fun moveEvent(
  move: Move,
  supplier: Supplier = Supplier.SERCO,
  type: String,
  occurredAt: LocalDateTime? = null,
) = Event(
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
  state: JourneyState = JourneyState.Completed,
  pickUp: LocalDateTime? = move.pickUpDateTime,
  dropOff: LocalDateTime? = move.dropOffOrCancelledDateTime,
) = Journey(
  billable = true,
  clientTimeStamp = move.pickUpDateTime?.toLocalDate()?.atStartOfDay() ?: startOfPreviousMonth.atStartOfDay(),
  dropOffDateTime = dropOff,
  effectiveYear = effectiveYearForDate(move.pickUpDateTime?.toLocalDate() ?: startOfPreviousMonth),
  fromNomisAgencyId = fromAgencyId,
  journeyId = UUID.randomUUID().toString(),
  moveId = move.moveId,
  notes = move.notes,
  pickUpDateTime = pickUp,
  state = state,
  supplier = supplier,
  toNomisAgencyId = toAgencyId,
  updatedAt = move.pickUpDateTime?.toLocalDate()?.atStartOfDay() ?: startOfPreviousMonth.atStartOfDay(),
  vehicleRegistration = "ABCDEFG",
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

private fun journeyStartEvent(
  journey: Journey,
  supplier: Supplier = Supplier.SERCO,
  details: Map<String, Any> = emptyMap(),
) = Event(
  details = details,
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

private fun journeyCompleteEvent(
  journey: Journey,
  supplier: Supplier = Supplier.SERCO,
  details: Map<String, Any> = emptyMap(),
) = Event(
  details = details,
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
    updated_at,
    details
  ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()

private val priceSql = """
  insert into prices (
    price_id,
    added_at,
    effective_year,
    price_in_pence,
    supplier,
    from_location_id,
    to_location_id
  ) values (?, ?, ?, ?, ?, ?, ?)
""".trimIndent()
