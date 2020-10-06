package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location

@Component
object ReportingParser {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Takes a list of files content, and for each line in each file convert from JSON to entity of type T
     * @param file List of jsonl files content to parse
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

    fun parseAsProfileIdToPersonId(profileFiles: List<String>): Map<String, String> {
        return read(profileFiles) { Profile.fromJson(it) }.associateBy(keySelector = { it.id }, valueTransform = { it.personId })
    }

    fun parseAsPersonIdToPerson(peopleFiles: List<String>): Map<String, Person> {
        return read(peopleFiles) { Person.fromJson(it) }.associateBy(Person::id)
    }

    fun parseAsMoves(moveFiles: List<String>, locations: List<Location>): Collection<Move> {
        val locationConverter = LocationConverter(locations)
        return read(moveFiles) { Move.fromJson(it, locationConverter) }.
        filter { MoveStatus.statuses.contains(it.status) }.
        associateBy(Move::id).values
    }


    fun parseAsMoveIdToJourneys(journeyFiles: List<String>, locations: List<Location>): Map<String, List<Journey>> {
        val locationConverter = LocationConverter(locations)
        return read(journeyFiles) { Journey.fromJson(it, locationConverter) }.
        filter { JourneyState.states.contains(it.state) }.
        associateBy(Journey::id).values.groupBy(Journey::moveId)
    }

    fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<Event>> {
        return read(eventFiles) { Event.fromJson(it) }.
        filter { EventType.types.contains(it.type) }.
        groupBy(Event::eventableId)
    }

    fun parseAll(moveFiles: List<String>, profileFiles: List<String>, peopleFiles: List<String>, journeyFiles: List<String>, eventFiles: List<String>, locations: List<Location> = listOf()): List<MoveReport> {
        val moves = parseAsMoves(moveFiles, locations)
        val profileId2PersonId = parseAsProfileIdToPersonId(profileFiles)
        val people = parseAsPersonIdToPerson(peopleFiles)
        val journeys = parseAsMoveIdToJourneys(journeyFiles, locations)
        val events = parseAsEventableIdToEvents(eventFiles)

        val movesWithJourneysAndEvents = moves.map { move ->
            MoveReport(
                    move = move,
                    person = if (move.profileId == null) null else people[profileId2PersonId[move.profileId]],
                    journeysWithEvents = journeys.getOrDefault(move.id, listOf()).map { journey ->
                        JourneyWithEvents(journey = journey, events = events.getOrDefault(journey.id, listOf()))
                    },
                    events = events.getOrDefault(move.id, listOf()
                    )
            )
        }
        return movesWithJourneysAndEvents
    }
}