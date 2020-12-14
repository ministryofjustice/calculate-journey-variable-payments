package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.effectiveYearForDate
import java.time.LocalDate

@Component
class MovePersister(private val moveRepository: MoveRepository, private val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(moves: List<Move>) {

        var counter = 1
        logger.info("Persisting ${moves.size} moves")

        moves.forEach { move ->
            Result.runCatching {

                val pickUp = Event.getLatestByType(move.events, EventType.MOVE_START)?.occurredAt
                val dropOff = Event.getLatestByType(move.events, EventType.MOVE_COMPLETE)?.occurredAt
                val cancelled = Event.getLatestByType(move.events, EventType.MOVE_CANCEL)?.occurredAt

                val maybeExistingMove = moveRepository.findById(move.moveId).orElse(null)

                val mergeMove = maybeExistingMove?.let { existingMove ->
                    // merge move events
                    val mergedMoveEvents = (existingMove.events + move.events).distinctBy { it.id }

                    // merge journeys and their events
                    val existingJourneys = existingMove.journeys
                    val newJourneys = move.journeys

                    val mergedJourneys =
                        existingJourneys.filterNot { ej -> newJourneys.any { ej.journeyId == it.journeyId } } +
                            newJourneys.map { nj ->
                                existingJourneys.find { it.journeyId == nj.journeyId }?.let {
                                    nj.copy(events = (nj.events + it.events).distinctBy { it.id }.toMutableSet())
                                } ?: nj
                            }.toMutableSet()

                    move.copy(
                            events = mergedMoveEvents.toMutableSet(),
                            journeys = mergedJourneys.toMutableSet())
                } ?: move

                with(mergeMove) {
                    val newMove = Move(
                        moveId = move.moveId,
                        profileId = profileId,
                        updatedAt = updatedAt,
                        supplier = supplier,
                        status = status,
                        moveType = mergeMove.moveType(),
                        reference = reference,
                        moveDate = moveDate,
                        fromNomisAgencyId = fromNomisAgencyId,
                        toNomisAgencyId = toNomisAgencyId,
                        pickUpDateTime = pickUp,
                        dropOffOrCancelledDateTime = dropOff ?: cancelled,
                            reportFromLocationType = reportFromLocationType,
                            reportToLocationType = reportToLocationType,
                        vehicleRegistration = move.journeys.withIndex().joinToString(separator = ", ") {
                            it.value.vehicleRegistration ?: ""
                        },
                        //notes = move.events.notes(),
                        prisonNumber = maybeExistingMove?.prisonNumber,
                        latestNomisBookingId = maybeExistingMove?.latestNomisBookingId,
                        dateOfBirth = maybeExistingMove?.dateOfBirth,
                        firstNames = maybeExistingMove?.firstNames,
                        lastName = maybeExistingMove?.lastName,
                        gender =  maybeExistingMove?.gender,
                        ethnicity = maybeExistingMove?.ethnicity,
                        journeys = mergeMove.journeys.map { addFieldsToJourney(moveDate, it) }.toMutableSet(),
                        events = mergeMove.events.toMutableSet()
                    )

                    moveRepository.save(newMove)

                    if (counter++ % 1000 == 0) {
                        logger.info("Persisted $counter moves out of ${moves.size} (flushing moves to the database).")
                        moveRepository.flush()
                    }
                }

            }.onFailure { logger.warn(it.message) }

            moveRepository.flush()
        }
    }


    fun addFieldsToJourney(moveDate: LocalDate?, journey: Journey): Journey {

        val pickUp = Event.getLatestByType(journey.events, EventType.JOURNEY_START)?.occurredAt
        val dropOff = Event.getLatestByType(journey.events, EventType.JOURNEY_COMPLETE)?.occurredAt

        return journey.copy(
            pickUpDateTime = pickUp,
            dropOffDateTime = dropOff,
            effectiveYear = pickUp?.year ?: effectiveYearForDate(moveDate ?: timeSource.date())
        )
    }
}

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING, EventType.MOVE_CANCEL).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.joinToString { "${it.type}: ${it.notes}" }
