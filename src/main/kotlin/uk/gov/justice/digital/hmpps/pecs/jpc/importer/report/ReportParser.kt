package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.price.equalsStringCaseInsensitive

@Component
object ReportParser {

    private val logger = LoggerFactory.getLogger(javaClass)

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

    fun parseAsPersonIdToProfileId(profileFiles: List<String>): Map<String, String> {
        logger.info("Parsing profiles")
        return read(profileFiles) { ReportProfile.fromJson(it) }.associateBy(keySelector = { it.personId }, valueTransform = { it.id })
    }

    fun parseAsPerson(peopleFiles: List<String>): Sequence<ReportPerson> {
        logger.info("Parsing people")
        return read(peopleFiles) { ReportPerson.fromJson(it) }
    }

    fun parseAsMoves(moveFiles: List<String>): Collection<ReportMove> {
        logger.info("Parsing moves")
        return read(moveFiles) { ReportMove.fromJson(it) }.
        associateBy(ReportMove::id).values // associateBy will only include latest Move by id
    }


    fun parseAsMoveIdToJourneys(journeyFiles: List<String>): Map<String, List<ReportJourney>> {
        logger.info("Parsing journeys")
        return read(journeyFiles) { ReportJourney.fromJson(it) }.
        filter {  (JourneyState.COMPLETED.equalsStringCaseInsensitive(it.state) || JourneyState.CANCELLED.equalsStringCaseInsensitive(it.state)) }.
        associateBy(ReportJourney::id).values.groupBy(ReportJourney::moveId) // associateBy will only include latest Journey by id
    }

    fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<Event>> {
        logger.info("Parsing events")
        return read(eventFiles) { Event.fromJson(it) }.
        filter { EventType.types.contains(it.type) }.
        distinctBy { it.id }. // filter duplicates (shouldn't be any, but just in case)
        groupBy(Event::eventableId)
    }

    fun parseMovesJourneysEvents(moveFiles: List<String>, journeyFiles: List<String>, eventFiles: List<String>): List<Report> {
        val moves = parseAsMoves(moveFiles)
        val journeys = parseAsMoveIdToJourneys(journeyFiles)
        val events = parseAsEventableIdToEvents(eventFiles)

        return moves.map { move ->
            Report(
                move = move,
                journeysWithEvents = journeys.getOrDefault(move.id, listOf()).map { journey -> JourneyWithEvents(reportJourney = journey, events = events.getOrDefault(journey.id, listOf())) },
                moveEvents = events.getOrDefault(move.id, listOf()))
        }
    }

    fun parsePeople(profileFiles: List<String>, peopleFiles: List<String>): Sequence<ReportPerson> {
        val personIdToProfileId = parseAsPersonIdToProfileId(profileFiles)
        val people = parseAsPerson(peopleFiles)

        // Return people with profile ids
        return people.map { it.copy(profileId = personIdToProfileId[it.id]) }.filterNot { it.profileId == null }
    }

}