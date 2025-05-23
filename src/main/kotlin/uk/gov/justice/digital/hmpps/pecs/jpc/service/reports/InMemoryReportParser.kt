package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<InMemoryReportParser>()

/**
 * We cannot guarantee the order in which moves, journeys and events come in and this matters when grouping the reports
 * together hence we read them all into memory first. This is not ideal as it requires a large memory footprint. Because
 * of the need to group data, streaming would not work here in its current form, so we would need to rethink how this
 * could be done.
 */
@Component
object InMemoryReportParser {

  fun parseMovesJourneysEvents(
    moveFiles: List<String>,
    journeyFiles: List<String>,
    eventFiles: List<String>,
  ): List<Move> {
    val moves = parseAsMoves(moveFiles)
    val journeys = parseAsMoveIdToJourneys(journeyFiles)
    val events = parseAsEventableIdToEvents(eventFiles)

    return moves.map { move ->
      move.copy(
        events = events.getOrDefault(move.moveId, listOf()),
        journeys = journeys.getOrDefault(move.moveId, listOf())
          .map { journey -> journey.copy(events = events.getOrDefault(journey.journeyId, listOf())) },
      )
    }
  }

  fun parseAsMoves(moveFiles: List<String>): Collection<Move> {
    logger.info("Parsing moves")
    return read(moveFiles) { Move.fromJson(it) }.associateBy(Move::moveId).values // associateBy will only include latest Move by id
  }

  fun parseAsMoveIdToJourneys(journeyFiles: List<String>): Map<String, List<Journey>> {
    logger.info("Parsing journeys")
    return read(journeyFiles) { Journey.fromJson(it) }.filter { (JourneyState.completed == it.state || JourneyState.cancelled == it.state) }
      .associateBy(Journey::journeyId).values.groupBy(Journey::moveId) // associateBy will only include latest Journey by id
  }

  fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<Event>> {
    logger.info("Parsing events")
    return read(eventFiles) { Event.fromJson(it) }.filter { EventType.types.contains(it.type) }
      .distinctBy { it.eventId } // filter duplicates (shouldn't be any, but just in case)
      .groupBy(Event::eventableId)
  }

  /**
   * Takes a list of files content, and for each line in each file convert from JSON to entity of type T
   * @param files List of jsonl files content to parse
   * @param f Lambda that takes a String and converts to entity of type T?
   * @return Sequence of entities of type T
   */
  private fun <T> read(files: List<String>, f: (j: String) -> T?): Sequence<T> = files.asSequence().flatMap {
    it.splitToSequence("\n").filter { it.isNotEmpty() }.map { json ->
      try {
        f(json)
      } catch (e: Exception) {
        // Simply warn and go on to the next one
        logger.warn("ERROR parsing json $json: ${e.message}")
        null
      }
    }
  }.filterNotNull()
}
