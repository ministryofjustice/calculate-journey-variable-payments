package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<ReportParser>()

@Component
object ReportParser {

  /**
   * Takes a list of files content, and for each line in each file convert from JSON to entity of type T
   * @param files List of jsonl files content to parse
   * @param f Lambda that takes a String and converts to entity of type T?
   * @return Sequence of entities of type T
   */
  fun <T> read(files: List<String>, f: (j: String) -> T?): Sequence<T> {
    return files.asSequence().flatMap {
      it.split("\n").filter { it.isNotEmpty() }.map { json ->
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

  fun parseAsPerson(peopleFiles: List<String>): Sequence<Person> {
    logger.info("Parsing people")
    return read(peopleFiles) { Person.fromJson(it) }
  }

  fun parseAsProfile(profileFiles: List<String>): Sequence<Profile> {
    logger.info("Parsing profiles")
    return read(profileFiles) { Profile.fromJson(it) }
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

  fun parseMovesJourneysEvents(
    moveFiles: List<String>,
    journeyFiles: List<String>,
    eventFiles: List<String>
  ): List<Move> {
    val moves = parseAsMoves(moveFiles)
    val journeys = parseAsMoveIdToJourneys(journeyFiles)
    val events = parseAsEventableIdToEvents(eventFiles)

    return moves.map { move ->
      move.copy(
        events = events.getOrDefault(move.moveId, listOf()),
        journeys = journeys.getOrDefault(move.moveId, listOf())
          .map { journey -> journey.copy(events = events.getOrDefault(journey.journeyId, listOf())) }
      )
    }
  }
}
