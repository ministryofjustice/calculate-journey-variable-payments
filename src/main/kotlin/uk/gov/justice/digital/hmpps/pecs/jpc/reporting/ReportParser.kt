package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.equalsStringCaseInsensitive

@Component
object ReportParser {

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
        logger.info("Parsing profiles")
        return read(profileFiles) { Profile.fromJson(it) }.associateBy(keySelector = { it.id }, valueTransform = { it.personId })
    }

    fun parseAsPersonIdToPerson(peopleFiles: List<String>): Map<String, Person> {
        logger.info("Parsing people")
        return read(peopleFiles) { Person.fromJson(it) }.associateBy(Person::id)
    }

    fun parseAsMoves(supplier: Supplier, moveFiles: List<String>): Collection<Move> {
        logger.info("Parsing moves")
        return read(moveFiles) { Move.fromJson(it) }.
        filter {
            supplier.equalsStringCaseInsensitive(it.supplier) &&
            (MoveStatus.COMPLETED.equalsStringCaseInsensitive(it.status) || MoveStatus.CANCELLED.equalsStringCaseInsensitive(it.status))
        }.
        associateBy(Move::id).values // associateBy will only include latest Move by id
    }


    fun parseAsMoveIdToJourneys(journeyFiles: List<String>): Map<String, List<Journey>> {
        logger.info("Parsing journeys")
        return read(journeyFiles) { Journey.fromJson(it) }.
        filter {  (JourneyState.COMPLETED.equalsStringCaseInsensitive(it.state) || JourneyState.CANCELLED.equalsStringCaseInsensitive(it.state)) }.
        associateBy(Journey::id).values.groupBy(Journey::moveId) // associateBy will only include latest Journey by id
    }

    fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<Event>> {
        logger.info("Parsing events")
        return read(eventFiles) { Event.fromJson(it) }.
        filter { EventType.types.contains(it.type) }.
        distinctBy { it.id }. // filter duplicates (shouldn't be any, but just in case)
        groupBy(Event::eventableId)
    }

    fun parseAll(supplier: Supplier, moveFiles: List<String>, profileFiles: List<String>, peopleFiles: List<String>, journeyFiles: List<String>, eventFiles: List<String>): List<Report> {
        val moves = parseAsMoves(supplier, moveFiles)
        val profileId2PersonId = parseAsProfileIdToPersonId(profileFiles)
        val people = parseAsPersonIdToPerson(peopleFiles)
        val journeys = parseAsMoveIdToJourneys(journeyFiles)
        val events = parseAsEventableIdToEvents(eventFiles)

        return moves.map { move ->
            Report(
                    move = move,
                    person = move.profileId?.let {people[profileId2PersonId[it]]},
                    journeysWithEvents = journeys.getOrDefault(move.id, listOf()).map { journey ->
                        JourneyWithEvents(journey = journey, events = events.getOrDefault(journey.id, listOf()))
                    },
                    events = events.getOrDefault(move.id, listOf()
                    )
            )
        }
    }

}