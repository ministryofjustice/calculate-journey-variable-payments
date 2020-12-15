package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class MovePersister(private val moveRepository: MoveRepository,
                    private val journeyRepository: JourneyRepository,
                    private val eventRepository: EventRepository,
                    private val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(moves: List<Move>) {

        var counter = 1
        logger.info("Persisting ${moves.size} moves")

        moves.forEach { newMove ->
            Result.runCatching {

                val moveId = newMove.moveId
                val maybeExistingMove = moveRepository.findById(newMove.moveId).orElse(null)

                val journeys = (newMove.journeys +
                        (maybeExistingMove?.let { journeyRepository.findAllByMoveId(moveId) }
                                ?: listOf())).distinctBy { it.journeyId }

                val moveEvents = (newMove.events +
                        (maybeExistingMove?.let { eventRepository.findAllByEventableId(moveId) }
                                ?: listOf())).distinctBy { it.eventId }

                val journeyEvents = (newMove.journeys.flatMap { it.events } +
                        (maybeExistingMove?.let { eventRepository.findByEventableIdIn(journeys.map { it.journeyId }) }
                                ?: listOf())).distinctBy { it.eventId }

                val pickUp = Event.getLatestByType(moveEvents, EventType.MOVE_START)?.occurredAt
                val dropOff = Event.getLatestByType(moveEvents, EventType.MOVE_COMPLETE)?.occurredAt
                val cancelled = Event.getLatestByType(moveEvents, EventType.MOVE_CANCEL)?.occurredAt

                val moveToSave = newMove.copy(
                        moveType = newMove.moveType(),
                        pickUpDateTime = pickUp,
                        dropOffOrCancelledDateTime = dropOff ?: cancelled,
                        vehicleRegistration = journeys.withIndex().joinToString(separator = ", ") {
                            it.value.vehicleRegistration ?: ""
                        },
                        notes = moveEvents.notes(),
                )

                moveRepository.save(moveToSave)

                val journeysToSave = processJourneys(moveToSave, journeys, journeyEvents)
                journeyRepository.saveAll(journeysToSave)

                eventRepository.saveAll(moveEvents + journeyEvents)

                if (counter++ % 1000 == 0) {
                    logger.info("Persisted $counter moves out of ${moves.size} (flushing moves to the database).")
                    moveRepository.flush()
                }

            }.onFailure { logger.warn("Error inserting $newMove" + it.message) }

            moveRepository.flush()
        }
    }


    fun processJourneys(move: Move, journeys:List<Journey>, journeyEvents: List<Event>): List<Journey> {
        return if (move.moveType() == MoveType.CANCELLED && journeys.isEmpty())
            listOf(fakeCancelledJourney(move)) // add a fake cancelled journey
        else
            journeys.map { journey ->
                val thisJourneyEvents = journeyEvents.filter { it.eventableId == journey.journeyId }
                val pickUp = Event.getLatestByType(thisJourneyEvents, EventType.JOURNEY_START)?.occurredAt
                val dropOff = Event.getLatestByType(thisJourneyEvents, EventType.JOURNEY_COMPLETE)?.occurredAt

                journey.copy(
                    events = listOf(),
                    pickUpDateTime = pickUp,
                    dropOffDateTime = dropOff,
                    effectiveYear = pickUp?.year ?: effectiveYearForDate(move.moveDate ?: timeSource.date())
                )
            }
    }

    fun fakeCancelledJourney(move: Move): Journey {
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
                effectiveYear = effectiveYearForDate(move.moveDate ?: LocalDate.now()),
                events = listOf()
        )
    }
}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }
