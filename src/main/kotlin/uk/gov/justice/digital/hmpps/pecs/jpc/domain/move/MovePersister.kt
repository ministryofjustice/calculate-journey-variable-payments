package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDateTime
import java.util.UUID

private val logger = loggerFor<MovePersister>()

@Component
class MovePersister(
  private val moveRepository: MoveRepository,
  private val journeyRepository: JourneyRepository,
  private val eventRepository: EventRepository,
  private val timeSource: TimeSource
) {

  /**
   * Returns the total number of successfully persisted moves.
   */
  fun persist(moves: List<Move>): Int {

    var counter = 0
    val movesToSave = mutableListOf<Move>()
    val journeysToSave = mutableListOf<Journey>()
    val eventsToSave = mutableListOf<Event>()
    logger.info("Persisting ${moves.size} moves")

    @Transactional
    fun save() {
      saveFlushAndClear(moveRepository, movesToSave)
      saveFlushAndClear(journeyRepository, journeysToSave)
      saveFlushAndClear(eventRepository, eventsToSave)
    }

    moves.chunked(100).forEach { hundredMoves ->

      Result.runCatching {

        val existingMoves = moveRepository.findAllByMoveIdIn(hundredMoves.map { it.moveId })
        val existingJourneys = journeyRepository.findAllByMoveIdIn(existingMoves.map { it.moveId })
        val existingMoveEvents = eventRepository.findAllByEventableIdIn(existingMoves.map { it.moveId })
        val existingJourneyEvents = eventRepository.findAllByEventableIdIn(existingJourneys.map { it.journeyId })

        val allJourneys = (hundredMoves.flatMap { it.journeys } + existingJourneys).distinctBy { it.journeyId }
        val allMoveEvents = (hundredMoves.flatMap { it.events } + existingMoveEvents).distinctBy { it.eventId }
        val allJourneyEvents =
          (hundredMoves.flatMap { it.journeys.flatMap { it.events ?: emptyList() } }) + existingJourneyEvents.distinctBy { it.eventId }

        val moveId2Journeys = allJourneys.groupBy { it.moveId }
        val moveId2MoveEvents = allMoveEvents.groupBy { it.eventableId }
        val journeyId2Events = allJourneyEvents.groupBy { it.eventableId }

        hundredMoves.forEach { newMove ->

          val moveId = newMove.moveId
          val journeys = moveId2Journeys.getOrDefault(moveId, listOf())
          val moveEvents = moveId2MoveEvents.getOrDefault(moveId, listOf())
          val journeyEvents = journeys.flatMap { journeyId2Events.getOrDefault(it.journeyId, listOf()) }

          val pickUp = Event.getLatestByType(moveEvents, EventType.MOVE_START)?.occurredAt
          val dropOff = Event.getLatestByType(moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
          val cancelled = Event.getLatestByType(moveEvents, EventType.MOVE_CANCEL)?.occurredAt

          // calculate move type
          val thisJourneyId2Events = journeyEvents.groupBy { it.eventableId }
          val journeysWithEvents = journeys.map {
            it.copy(events = thisJourneyId2Events[it.journeyId] ?: listOf())
          }
          // This is just used to calculated moveType
          val moveWithJourneysAndEvents = newMove.copy(
            events = moveEvents,
            journeys = journeysWithEvents
          )

          val moveToSave = newMove.copy(
            moveType = moveWithJourneysAndEvents.moveType(),
            events = moveEvents,
            journeys = journeysWithEvents,
            pickUpDateTime = pickUp,
            dropOffOrCancelledDateTime = dropOff ?: cancelled,
            notes = moveEvents.notes(),
          )

          movesToSave += moveToSave

          val moveJourneysToSave = processJourneys(moveToSave, journeys, journeyEvents)
          journeysToSave += moveJourneysToSave

          eventsToSave += moveEvents + journeyEvents

          Result.runCatching { save() }
            .onSuccess { counter++ }
            .onFailure {
              logger.error("Error inserting move '${moveToSave.reference}': ${it.stackTraceToString()}")

              movesToSave.clear()
              journeysToSave.clear()
              eventsToSave.clear()

              if (counter > 0) counter--
            }

          if (counter % 500 == 0) {
            logger.info("Persisted $counter moves ...")
          }
        }
      }.onFailure { logger.warn("Error inserting moves: ${it.message}") }
    }

    logger.info("Persisted $counter moves out of total ${moves.size}.")

    return counter
  }

  fun processJourneys(move: Move, journeys: List<Journey>, journeyEvents: List<Event>): List<Journey> {
    return if (move.moveType() == MoveType.CANCELLED && journeys.isEmpty())
      listOf(fakeCancelledJourneyForPricing(move))
    else
      journeys.map { journey ->
        val thisJourneyEvents = journeyEvents.filter { it.eventableId == journey.journeyId }
        val pickUp = Event.getLatestByType(thisJourneyEvents, EventType.JOURNEY_START)?.occurredAt
        val dropOff = Event.getLatestByType(thisJourneyEvents, EventType.JOURNEY_COMPLETE)?.occurredAt
        val vehicleRegistration = journey.determineRegistrationsCombinedWithThoseFrom(thisJourneyEvents)

        journey.copy(
          events = listOf(),
          pickUpDateTime = pickUp,
          dropOffDateTime = dropOff,
          effectiveYear = pickUp?.let { effectiveYearForDate(it.toLocalDate()) } ?: effectiveYearForDate(
            move.moveDate ?: timeSource.date()
          ),
          vehicleRegistration = vehicleRegistration
        )
      }
  }

  private fun Journey.determineRegistrationsCombinedWithThoseFrom(events: List<Event>) = this.copy(events = events).vehicleRegistrations()

  /**
   * There ane no actual supplier journey/journey events for billable cancelled moves so to price a cancelled move we
   * must create a fake journey.
   */
  internal fun fakeCancelledJourneyForPricing(move: Move): Journey {
    return Journey(
      journeyId = UUID.randomUUID().toString(),
      updatedAt = LocalDateTime.now(),
      moveId = move.moveId,
      billable = true,
      supplier = move.supplier,
      clientTimeStamp = LocalDateTime.now(),
      fromNomisAgencyId = move.fromNomisAgencyId,
      toNomisAgencyId = move.toNomisAgencyId!!,
      state = JourneyState.cancelled,
      vehicleRegistration = null,
      notes = "FAKE JOURNEY ADDED FOR CANCELLED BILLABLE MOVE",
      effectiveYear = effectiveYearForDate(move.moveDate ?: timeSource.date()),
      events = listOf()
    )
  }
}

private val noteworthyEvents = listOf(
  EventType.MOVE_REDIRECT,
  EventType.MOVE_LOCKOUT,
  EventType.JOURNEY_CANCEL,
  EventType.MOVE_LODGING_START,
  EventType.MOVE_LODGING_END,
  EventType.JOURNEY_LODGING,
  EventType.MOVE_CANCEL
).map { it.value }

fun List<Event>.notes() =
  filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }
